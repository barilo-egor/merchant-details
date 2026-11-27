package net.rcetech.md.integrations.platform.tronex;

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
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.util.MultiValueMap;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.TronExProperties;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов tronex без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт tronex попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class TronExAccountServiceTest {

    private static final String TEST_API_KEY = "tronex_sim_secret_key_12345";

    private static final String GATEWAY_URL = "https://exchange.com";

    private static final String CALLBACK_SECRET = "secret_callback_code";

    @Mock
    private RestClient restClient;

    @Mock
    private TronExProperties tronExProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<String> bodyCaptor;

    private TronExOrderCreationService service;

    @BeforeEach
    void setUp() {
        service = service(Merchant.TRON_EX_SIM);
        ReflectionTestUtils.setField(service, "requestService", requestService);
        ReflectionTestUtils.setField(service, "objectMapper", JsonMappers.create());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertThat(service(merchant).getMerchant()).isEqualTo(merchant);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void uriBuilderShouldBuildCorrectPath(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

        Function<UriBuilder, URI> builderFunction = service(merchant).uriBuilder(detailsRequest);
        URI resultUri = builderFunction.apply(uriBuilder);

        assertThat(resultUri.getPath()).isEqualTo("/incoming/payment/create/");
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void headersShouldAddRequiredHeaders(Merchant merchant) {
        when(tronExProperties.key()).thenReturn(TEST_API_KEY);
        HttpHeaders httpHeaders = new HttpHeaders();

        service(merchant).headers(new OrderCreationRequest(), null).accept(httpHeaders);

        assertThat(httpHeaders.get("Content-Type")).containsExactly("application/json");
        assertThat(httpHeaders.get("X-Api-Key")).containsExactly(TEST_API_KEY);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void bodyShouldBuildRequestCorrectly(Merchant merchant) {
        when(callbackConfig.getGatewayUrl()).thenReturn(GATEWAY_URL);
        when(callbackConfig.getCallbackSecret()).thenReturn(CALLBACK_SECRET);

        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod("SIM");

        Request request = service(merchant).body(detailsRequest);

        assertThat(request.getOrderId()).isNotNull();
        assertThat(request.getAmount()).isEqualTo(1000);
        assertThat(request.getMethod()).isEqualTo(Method.SIM);
        assertThat(request.getCallbackUrl()).isEqualTo(
                GATEWAY_URL + "/merchant-details/callback?merchant=" + merchant.name() + "&secret=" + CALLBACK_SECRET
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void buildResponseShouldMapNonQrResponseCorrectly(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();

        data.setId("trade-sim-12345");
        data.setAmount("5000");
        data.setBank("MTS");
        data.setNumber("+79991234567");
        data.setMethod(Method.SIM);
        response.setPlatform(data);

        Optional<DetailsResponse> optionalDetailsResponse = service(merchant).buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("trade-sim-12345");
        assertThat(detailsResponse.getAmount()).isEqualTo(5000);
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("CREATED");
        assertThat(detailsResponse.getBank()).isEqualTo("MTS");
        assertThat(detailsResponse.getDetails()).isEqualTo("+79991234567");
        assertThat(detailsResponse.getQr()).isNull();
        assertThat(detailsResponse.getMerchant()).isEqualTo(merchant);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"TRON_EX_BT", "TRON_EX_PDF", "TRON_EX_QR", "TRON_EX_SIM"})
    void buildResponseShouldMapQrResponseCorrectly(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();

        data.setId("trade-qr-67890");
        data.setAmount("2500");
        data.setPaymentUrl("https://pay.tronex.io/qr/67890");
        data.setMethod(Method.SBER_QR);
        response.setPlatform(data);

        Optional<DetailsResponse> optionalDetailsResponse = service(merchant).buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("trade-qr-67890");
        assertThat(detailsResponse.getAmount()).isEqualTo(2500);
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("CREATED");
        assertThat(detailsResponse.getQr()).isEqualTo("https://pay.tronex.io/qr/67890");
        assertThat(detailsResponse.getBank()).isNull();
        assertThat(detailsResponse.getDetails()).isNull();
        assertThat(detailsResponse.getMerchant()).isEqualTo(merchant);
    }

    @Test
    void makeCancelRequestShouldCallRequestServiceWithCorrectParams() {
        String orderId = "order-cancel-sim-999";
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);

        when(tronExProperties.key()).thenReturn(TEST_API_KEY);

        service.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(restClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                bodyCaptor.capture()
        );

        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();
        URI generatedUri = uriBuilderCaptor.getValue().apply(uriBuilder);
        assertThat(generatedUri.getPath()).isEqualTo("/incoming/payment/cancel");

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertThat(headers.get("X-Api-Key")).containsExactly(TEST_API_KEY);
        assertThat(headers.get("Content-Type")).containsExactly("application/json");

        assertThat(bodyCaptor.getValue()).isEqualTo("{\"id\":\"" + orderId + "\"}");
    }

    @Test
    void sendReceiptShouldCallRequestServiceWithMultipartData() {
        String orderId = "order-sim-receipt-123";
        byte[] fileContent = "dummy receipt content".getBytes();
        String fileName = "receipt.png";

        when(tronExProperties.key()).thenReturn(TEST_API_KEY);

        service.sendReceipt(orderId, fileContent, fileName);

        verify(requestService).requestAsync(
                eq(restClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                org.mockito.ArgumentMatchers.any(MultiValueMap.class),
                org.mockito.ArgumentMatchers.any()
        );

        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();
        URI generatedUri = uriBuilderCaptor.getValue().apply(uriBuilder);
        assertThat(generatedUri.getPath()).isEqualTo("/payment/upload/check/");

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertThat(headers.get("X-Api-Key")).containsExactly(TEST_API_KEY);
    }

    private TronExOrderCreationService service(Merchant merchant) {
        return new TronExOrderCreationService(merchant, restClient, tronExProperties, callbackConfig);
    }
}
