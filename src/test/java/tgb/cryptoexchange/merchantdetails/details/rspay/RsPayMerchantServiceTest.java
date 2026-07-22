package tgb.cryptoexchange.merchantdetails.details.rspay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.RSPayImplProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RsPayMerchantServiceTest {

    private static final String API_KEY = "test-api-key";
    private static final String SECRET = "test-secret";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private RSPayImplProperties rsPayProperties;
    @InjectMocks
    private RSPayOrderCreationServiceImpl rsPayService;
    @Mock
    private CallbackConfig callbackConfig;
    @Mock
    private RequestService requestService;
    @Mock
    private WebClient webClient;
    @Mock
    private SignatureService signatureService;
    private DetailsRequest detailsRequest;
    private Response response;
    private CancelOrderRequest cancelOrderRequest;

    @BeforeEach
    void setUp() {
        rsPayService = new RSPayOrderCreationServiceImpl(
                webClient,
                rsPayProperties,
                callbackConfig,
                signatureService
        );
        rsPayService.setObjectMapper(objectMapper);
        rsPayService.setRequestService(requestService);

        detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(100);
        detailsRequest.setCurrentMerchantMethod("CARD");

        response = new Response();
        response.setId("order-123");
        response.setStatus(Status.PENDING);

        cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId("order-123");
    }

    @Test
    void shouldCreateOrderWithPaymentLink() {
        Response.Requisites requisites = new Response.Requisites();
        requisites.setPaymentLink("https://pay.test.com/link");
        response.setRequisites(requisites);


        String responseJson = "{\"merchant_transaction_id\":\"order-123\",\"status\":\"pending\",\"requisites\":{\"payment_link\":\"https://pay.test.com/link\"}}";
        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = rsPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.RS_PAY);
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("order-123");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
        assertThat(detailsResponse.getQr()).isEqualTo("https://pay.test.com/link");
        assertThat(detailsResponse.getDetails()).isNull();
    }

    @Test
    void shouldCreateOrderWithMobileProviderDetails() {
        Response.Requisites requisites = new Response.Requisites();
        requisites.setMobileProvider("MTS");
        requisites.setPhoneNumber("+7-999-123-4567");
        response.setRequisites(requisites);

        String responseJson = "{\"merchant_transaction_id\":\"order-123\",\"status\":\"pending\",\"requisites\":{\"mobile_provider\":\"MTS\",\"phone_number\":\"+7-999-123-4567\"}}";
        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = rsPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getDetails()).isEqualTo("MTS +7-999-123-4567");
        assertThat(detailsResponse.getQr()).isNull();
    }

    @Test
    void shouldCreateOrderWithCardDetails() {
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Sberbank");
        requisites.setCardNumber("**** 1234");
        response.setRequisites(requisites);

        String responseJson = "{\"merchant_transaction_id\":\"order-123\",\"status\":\"pending\",\"requisites\":{\"bank_name\":\"Sberbank\",\"card_number\":\"**** 1234\"}}";
        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = rsPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getDetails()).isEqualTo("Sberbank **** 1234");
    }

    @Test
    void shouldCreateOrderWithBankAndPhoneDetails() {
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName("Tinkoff");
        requisites.setPhoneNumber("+7-999-888-7766");
        response.setRequisites(requisites);

        String responseJson = "{\"merchant_transaction_id\":\"order-123\",\"status\":\"pending\",\"requisites\":{\"bank_name\":\"Tinkoff\",\"phone_number\":\"+7-999-888-7766\"}}";
        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = rsPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getDetails()).isEqualTo("Tinkoff +7-999-888-7766");
    }


    @Test
    void shouldCancelOrderSuccessfully() {
        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn("{\"status\":\"cancelled\"}");

        rsPayService.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        );
    }

    @Test
    void shouldAddCorrectHeadersForCancel() {
        when(rsPayProperties.apiKey()).thenReturn(API_KEY);
        when(rsPayProperties.secret()).thenReturn(SECRET);
        when(signatureService.hmacSHA256(anyString(), eq(SECRET))).thenReturn("cancel-signature");

        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn("{\"status\":\"cancelled\"}");

        rsPayService.makeCancelRequest(cancelOrderRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                headersCaptor.capture(),
                anyString()
        );

        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);

        assertThat(headers.getFirst("X-Signature")).isEqualTo("cancel-signature");
        assertThat(headers.getFirst("X-Shop-API-Key")).isEqualTo(API_KEY);
        assertThat(headers.getFirst("X-Nonce")).isNotNull();
        assertThat(headers.getFirst("X-Timestamp")).isNotNull();
    }

    @Test
    void getMerchantShouldReturnPayLee() {
        assertEquals(Merchant.RS_PAY, rsPayService.getMerchant());
    }

}
