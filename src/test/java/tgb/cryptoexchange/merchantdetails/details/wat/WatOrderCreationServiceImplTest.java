package tgb.cryptoexchange.merchantdetails.details.wat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.WatPropertiesImpl;
import tgb.cryptoexchange.merchantdetails.service.ReceiptService;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatOrderCreationServiceImplTest {

    private static final String TEST_TOKEN = "wat_secret_token_12345";

    @Mock
    private WebClient webClient;

    @Mock
    private WatPropertiesImpl watProperties;

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

    private WatOrderCreationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WatOrderCreationServiceImpl(webClient, watProperties, callbackConfig, receiptService);
        ReflectionTestUtils.setField(service, "requestService", requestService);
    }


    @Test
    void getMerchant_ShouldReturnWat() {
        assertThat(service.getMerchant()).isEqualTo(Merchant.WAT);
    }

    @Test
    void uriBuilder_ShouldBuildCorrectPath() {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

        Function<UriBuilder, URI> builderFunction = service.uriBuilder(detailsRequest);
        URI resultUri = builderFunction.apply(uriBuilder);

        assertThat(resultUri.getPath()).isEqualTo("/orders");
    }

    @Test
    void headers_ShouldAddRequiredHeaders() {
        when(watProperties.token()).thenReturn(TEST_TOKEN);
        HttpHeaders httpHeaders = new HttpHeaders();

        service.headers(new OrderCreationRequest(), null).accept(httpHeaders);

        assertThat(httpHeaders.get("Content-Type")).containsExactly("application/json");
        assertThat(httpHeaders.get("Authorization")).containsExactly("Bearer " + TEST_TOKEN);
    }

    @Test
    void buildResponse_ShouldMapResponseCorrectly() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Data.BankInfo bankInfo = new Response.Data.BankInfo();

        bankInfo.setName("Sberbank");
        data.setId("order-wat-777");
        data.setRequisite("+79991234567");
        data.setStatus(Status.AWAITING_PAYMENT);
        data.setBank(bankInfo);
        response.setData(data);

        Optional<DetailsResponse> optionalDetailsResponse = service.buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("order-wat-777");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("AWAITING_PAYMENT");
        assertThat(detailsResponse.getDetails()).isEqualTo("Sberbank +79991234567");
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.WAT);
    }


    @Test
    void makeCancelRequest_ShouldCallRequestServiceWithCorrectParams() {
        String orderId = "order-cancel-999";
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);

        when(watProperties.token()).thenReturn(TEST_TOKEN);

        service.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(webClient),
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

}
