package tgb.cryptoexchange.merchantdetails.details.baypay;

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
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.BayPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BayPayOrderCreationServiceTest {

    private final UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();
    @Mock
    private WebClient webClient;
    @Mock
    private BayPayProperties bayPayProperties;
    @Mock
    private CallbackConfig callbackConfig;
    @Mock
    private RequestService requestService;
    @InjectMocks
    private BayPayOrderCreationService service;

    @BeforeEach
    void setUp() {
        service.setRequestService(requestService);
    }

    @Test
    void shouldReturnBayPayMerchant() {
        assertEquals(Merchant.BAY_PAY, service.getMerchant());
    }

    @Test
    void shouldBuildCorrectCreationUri() {
        DetailsRequest detailsRequest = new DetailsRequest();

        Function<UriBuilder, URI> builderFunction = service.uriBuilder(detailsRequest);
        URI uri = builderFunction.apply(uriBuilder);

        assertEquals("/s2s/invoices/create/", uri.toString());
    }

    @Test
    void shouldPopulateRequiredHeaders() {
        when(bayPayProperties.apiKey()).thenReturn("test-secret-api-key");

        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = service.headers(new DetailsRequest(), null);
        headersConsumer.accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals("Bearer test-secret-api-key", headers.getFirst("Authorization"))
        );
    }

    @Test
    void shouldBuildRequestBodyCorrectly() {
        when(callbackConfig.getGatewayUrl()).thenReturn("https://api.exchange.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("cb_secret_xyz");

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(15000);
        detailsRequest.setCurrentMerchantMethod(Method.CARD.name());

        Request actual = service.body(detailsRequest);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(15000, actual.getAmount()),
                () -> assertEquals(Method.CARD, actual.getMethod()),
                () -> assertNotNull(actual.getOrderId()),
                () -> assertEquals(36, actual.getOrderId().length()),
                () -> assertEquals(
                        "https://api.exchange.com/merchant-details/callback?merchant=BAY_PAY&secret=cb_secret_xyz",
                        actual.getCallbackUrl()
                )
        );
    }

    @Test
    void shouldBuildDetailsResponseFromGatewayResponse() {
        Response gatewayResponse = new Response();
        Response.Data data = new Response.Data();
        data.setId("inv_987456");
        data.setStatus(Status.ACTIVE);
        data.setAmount("25000");

        Response.Data.PaymentDetail paymentDetail = new Response.Data.PaymentDetail();
        paymentDetail.setBank("Сбербанк");
        paymentDetail.setDetail("2202202200001111");
        data.setPaymentDetail(paymentDetail);

        gatewayResponse.setData(data);

        Optional<DetailsResponse> actualOptional = service.buildResponse(gatewayResponse);

        assertTrue(actualOptional.isPresent());
        DetailsResponse actual = actualOptional.get();

        assertAll(
                () -> assertEquals(Merchant.BAY_PAY, actual.getMerchant()),
                () -> assertEquals("ACTIVE", actual.getMerchantOrderStatus()),
                () -> assertEquals("inv_987456", actual.getMerchantOrderId()),
                () -> assertEquals(25000, actual.getAmount()),
                () -> assertEquals("Сбербанк 2202202200001111", actual.getDetails())
        );
    }

    @Test
    void shouldMakeCorrectCancelRequest() {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId("baypay_order_777");

        service.makeCancelRequest(cancelOrderRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriCaptor.capture(),
                any(),
                isNull()
        );

        URI uri = uriCaptor.getValue().apply(uriBuilder);
        assertEquals("/s2s/invoice/baypay_order_777/cancel/", uri.toString());
    }
}