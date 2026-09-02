package tgb.cryptoexchange.merchantdetails.details.tronex;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.TronExSimProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TronExSimCreationServiceTest {

    private static final String TEST_API_KEY = "tronex_sim_secret_key_12345";

    private static final String GATEWAY_URL = "https://exchange.com";

    private static final String CALLBACK_SECRET = "secret_callback_code";

    @Mock
    private WebClient webClient;

    @Mock
    private TronExSimProperties tronExSimProperties;

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

    private TronExSimCreationService service;

    @BeforeEach
    void setUp() {
        service = new TronExSimCreationService(webClient, tronExSimProperties, callbackConfig);
        ReflectionTestUtils.setField(service, "requestService", requestService);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    @Test
    void getMerchant_ShouldReturnTronExSim() {
        assertThat(service.getMerchant()).isEqualTo(Merchant.TRON_EX_SIM);
    }

    @Test
    void uriBuilder_ShouldBuildCorrectPath() {
        DetailsRequest detailsRequest = new DetailsRequest();
        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

        Function<UriBuilder, URI> builderFunction = service.uriBuilder(detailsRequest);
        URI resultUri = builderFunction.apply(uriBuilder);

        assertThat(resultUri.getPath()).isEqualTo("/incoming/payment/create/");
    }

    @Test
    void headers_ShouldAddRequiredHeaders() {
        when(tronExSimProperties.key()).thenReturn(TEST_API_KEY);
        HttpHeaders httpHeaders = new HttpHeaders();

        service.headers(new DetailsRequest(), null).accept(httpHeaders);

        assertThat(httpHeaders.get("Content-Type")).containsExactly("application/json");
        assertThat(httpHeaders.get("X-Api-Key")).containsExactly(TEST_API_KEY);
    }

    @Test
    void body_ShouldBuildRequestCorrectly() {
        when(callbackConfig.getGatewayUrl()).thenReturn(GATEWAY_URL);
        when(callbackConfig.getCallbackSecret()).thenReturn(CALLBACK_SECRET);

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setCurrentMerchantMethod("SIM");

        Request request = service.body(detailsRequest);

        assertThat(request.getOrderId()).isNotNull();
        assertThat(request.getAmount()).isEqualTo(1000);
        assertThat(request.getMethod()).isEqualTo(Method.SIM);
        assertThat(request.getCallbackUrl()).isEqualTo(
                GATEWAY_URL + "/merchant-details/callback?merchant=TRON_EX_SIM&secret=" + CALLBACK_SECRET
        );
    }

    @Test
    void buildResponse_ShouldMapNonQrResponseCorrectly() {
        Response response = new Response();
        Response.Data data = new Response.Data();

        data.setId("trade-sim-12345");
        data.setAmount("5000");
        data.setBank("MTS");
        data.setNumber("+79991234567");
        data.setMethod(Method.SIM);
        response.setPlatform(data);

        Optional<DetailsResponse> optionalDetailsResponse = service.buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("trade-sim-12345");
        assertThat(detailsResponse.getAmount()).isEqualTo(5000);
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("CREATED");
        assertThat(detailsResponse.getDetails()).isEqualTo("MTS +79991234567");
        assertThat(detailsResponse.getQr()).isNull();
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.TRON_EX_SIM);
    }

    @Test
    void buildResponse_ShouldMapQrResponseCorrectly() {
        Response response = new Response();
        Response.Data data = new Response.Data();

        data.setId("trade-qr-67890");
        data.setAmount("2500");
        data.setPaymentUrl("https://pay.tronex.io/qr/67890");
        data.setMethod(Method.QR);
        response.setPlatform(data);

        Optional<DetailsResponse> optionalDetailsResponse = service.buildResponse(response);

        assertThat(optionalDetailsResponse).isPresent();
        DetailsResponse detailsResponse = optionalDetailsResponse.get();

        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("trade-qr-67890");
        assertThat(detailsResponse.getAmount()).isEqualTo(2500);
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("CREATED");
        assertThat(detailsResponse.getQr()).isEqualTo("https://pay.tronex.io/qr/67890");
        assertThat(detailsResponse.getDetails()).isNull();
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.TRON_EX_SIM);
    }

    @Test
    void makeCancelRequest_ShouldCallRequestServiceWithCorrectParams() {
        String orderId = "order-cancel-sim-999";
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);

        when(tronExSimProperties.key()).thenReturn(TEST_API_KEY);

        service.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(webClient),
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
    void sendReceipt_ShouldCallRequestServiceWithMultipartData() {
        String orderId = "order-sim-receipt-123";
        byte[] fileContent = "dummy receipt content".getBytes();
        String fileName = "receipt.png";

        when(tronExSimProperties.key()).thenReturn(TEST_API_KEY);

        service.sendReceipt(orderId, fileContent, fileName);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                org.mockito.ArgumentMatchers.<org.springframework.web.reactive.function.BodyInserters.MultipartInserter>any(),
                org.mockito.ArgumentMatchers.any()
        );

        UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();
        URI generatedUri = uriBuilderCaptor.getValue().apply(uriBuilder);
        assertThat(generatedUri.getPath()).isEqualTo("/payment/upload/check/");

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertThat(headers.get("X-Api-Key")).containsExactly(TEST_API_KEY);
    }

}