package net.rcetech.md.integrations.platform.wat;

import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.WatProperties;
import net.rcetech.md.integrations.service.ReceiptService;
import net.rcetech.md.integrations.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Общий тест аккаунтов wat без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт wat попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class WatAccountServiceTest {

    private static final String TEST_TOKEN = "wat_secret_token_12345";

    @Mock
    private RestClient restClient;

    @Mock
    private WatProperties watProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    @Mock
    private ReceiptService receiptService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<Collection<String>> folderNamesCaptor;

    private WatOrderCreationService service;

    @BeforeEach
    void setUp() {
        service = service(Merchant.WAT);
        ReflectionTestUtils.setField(service, "requestService", requestService);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertThat(service(merchant).getMerchant()).isEqualTo(merchant);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void uriBuilderShouldBuildCorrectPath(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

        Function<UriBuilder, URI> builderFunction = service(merchant).uriBuilder(detailsRequest);
        URI resultUri = builderFunction.apply(uriBuilder);

        assertThat(resultUri.getPath()).isEqualTo("/orders");
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void headersShouldAddRequiredHeaders(Merchant merchant) {
        when(watProperties.token()).thenReturn(TEST_TOKEN);
        HttpHeaders httpHeaders = new HttpHeaders();

        service(merchant).headers(new OrderCreationRequest(), null).accept(httpHeaders);

        assertThat(httpHeaders.get("Content-Type")).containsExactly("application/json");
        assertThat(httpHeaders.get("Authorization")).containsExactly("Bearer " + TEST_TOKEN);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void buildResponseShouldMapResponseCorrectly(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Data.BankInfo bankInfo = new Response.Data.BankInfo();

        bankInfo.setName("Sberbank");
        data.setId("order-wat-777");
        data.setRequisite("+79991234567");
        data.setStatus(Status.AWAITING_PAYMENT);
        data.setBank(bankInfo);
        response.setData(data);

        Optional<DetailsResponse> optionalDetailsResponse = service(merchant).buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("order-wat-777");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("AWAITING_PAYMENT");
        assertThat(detailsResponse.getBank()).isEqualTo("Sberbank");
        assertThat(detailsResponse.getDetails()).isEqualTo("+79991234567");
        assertThat(detailsResponse.getMerchant()).isEqualTo(merchant);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void makeCancelRequestShouldCallRequestServiceWithCorrectParams(Merchant merchant) {
        String orderId = "order-cancel-999";
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);

        when(watProperties.token()).thenReturn(TEST_TOKEN);

        WatOrderCreationService merchantService = service(merchant);
        ReflectionTestUtils.setField(merchantService, "requestService", requestService);
        merchantService.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(restClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                isNull()
        );

        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();
        URI generatedUri = uriBuilderCaptor.getValue().apply(uriBuilder);
        assertThat(generatedUri.getPath()).isEqualTo("/orders/" + orderId + "/cancel");

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertThat(headers.get("Authorization")).containsExactly("Bearer " + TEST_TOKEN);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void deleteReceiptFinalStatusShouldDeleteReceiptFromAllFamilyFolders(Merchant merchant) {
        service(merchant).deleteReceipt("order-wat-777", Status.FINISHED.name());

        verify(receiptService).deleteReceipt(eq("order-wat-777"), folderNamesCaptor.capture());
        assertThat(folderNamesCaptor.getValue()).containsExactlyInAnyOrder("wat", "wat_pdf", "wat_sim");
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void deleteReceiptCanceledStatusShouldDeleteReceiptFromAllFamilyFolders(Merchant merchant) {
        service(merchant).deleteReceipt("order-wat-777", Status.CANCELED.name());

        verify(receiptService).deleteReceipt(eq("order-wat-777"), anyCollection());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"WAT", "WAT_PDF", "WAT_SIM"})
    void deleteReceiptNonFinalStatusShouldNotDeleteReceipt(Merchant merchant) {
        service(merchant).deleteReceipt("order-wat-777", Status.CLAIM.name());

        verify(receiptService, never()).deleteReceipt(anyString(), anyString());
        verify(receiptService, never()).deleteReceipt(anyString(), anyCollection());
    }

    private WatOrderCreationService service(Merchant merchant) {
        return new WatOrderCreationService(merchant, restClient, watProperties, callbackConfig, receiptService);
    }
}
