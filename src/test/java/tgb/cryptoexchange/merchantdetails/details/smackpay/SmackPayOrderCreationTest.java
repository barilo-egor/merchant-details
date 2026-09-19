package tgb.cryptoexchange.merchantdetails.details.smackpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.SmackPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmackPayOrderCreationTest {

    private static final String API_KEY = "test-api-key";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WebClient webClient;

    @Mock
    private SmackPayProperties smackPayProperties;

    @Mock
    private RequestService requestService;

    private SmackPayOrderCreationService smackPayService;

    private DetailsRequest detailsRequest;

    @BeforeEach
    void setUp() {
        smackPayService = new TestSmackPayOrderCreationService(
                webClient,
                smackPayProperties
        );
        smackPayService.setObjectMapper(objectMapper);
        smackPayService.setRequestService(requestService);

        detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1500);
        detailsRequest.setRedirectUrl("https://mysite.com/payment/return");
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        String responseJson = """
                {
                  "id": "smack-order-777",
                  "amount": 1500,
                  "status": "pending",
                  "payment_url": "https://pay.smackpay.com/checkout/smack-order-777"
                }
                """;

        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = smackPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.SMACK_PAY);
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("smack-order-777");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
        assertThat(detailsResponse.getDetails()).isEqualTo("https://pay.smackpay.com/checkout/smack-order-777");
        assertThat(detailsResponse.getAmount()).isEqualTo(1500);
    }

    @Test
    void shouldBuildCorrectRequestBody() {
        Request request = smackPayService.body(detailsRequest);

        assertNotNull(request);
        assertEquals(1500, request.getAmount());
        assertEquals("https://mysite.com/payment/return", request.getReturnUrl());
        assertEquals("RUB", request.getCurrency());
        assertNotNull(request.getMetadata());
        assertNotNull(request.getMetadata().getOrderId());
        assertNotNull(request.getMetadata().getVisitorId());
    }

    @Test
    void shouldSetCorrectHeaders() {
        when(smackPayProperties.apiKey()).thenReturn(API_KEY);

        String responseJson = """
                {
                  "id": "smack-order-777",
                  "amount": 1500,
                  "status": "pending",
                  "payment_url": "https://pay.smackpay.com/checkout/smack-order-777"
                }
                """;

        when(requestService.request(
                eq(webClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        smackPayService.createOrder(detailsRequest);

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

        assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer " + API_KEY);
        assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
    }

    @Test
    void getMerchantShouldReturnSmackPay() {
        assertEquals(Merchant.SMACK_PAY, smackPayService.getMerchant());
    }

    private static class TestSmackPayOrderCreationService extends SmackPayOrderCreationService {
        protected TestSmackPayOrderCreationService(WebClient webClient,
                                                   SmackPayProperties smackPayProperties) {
            super(webClient, smackPayProperties);
        }
    }
}
