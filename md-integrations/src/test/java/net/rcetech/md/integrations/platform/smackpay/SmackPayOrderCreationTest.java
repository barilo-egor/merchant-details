package net.rcetech.md.integrations.platform.smackpay;

import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.SmackPayProperties;
import net.rcetech.md.integrations.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmackPayOrderCreationTest {

    private static final String API_KEY = "test-api-key";
    private static final String USER_ID = "123456789";
    private static final String CLIENT_ID = "987654321";

    private final ObjectMapper objectMapper = JsonMappers.create();

    @Mock
    private RestClient restClient;

    @Mock
    private SmackPayProperties smackPayProperties;

    @Mock
    private RequestService requestService;

    @Mock
    private SmackPayClientIds clientIds;

    private SmackPayOrderCreationService smackPayService;

    private OrderCreationRequest detailsRequest;

    @BeforeEach
    void setUp() {
        smackPayService = new TestSmackPayOrderCreationService(
                restClient,
                smackPayProperties,
                clientIds
        );
        smackPayService.setObjectMapper(objectMapper);
        smackPayService.setRequestService(requestService);

        detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(Method.NSPK_QR.name());
        detailsRequest.setAmount(1500);
        detailsRequest.setRedirectUrl("https://mysite.com/payment/return");
        detailsRequest.setUserId(USER_ID);
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
                eq(restClient),
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
        when(clientIds.findOrAssignClientId(USER_ID)).thenReturn(Optional.of(CLIENT_ID));

        Request request = smackPayService.body(detailsRequest);

        assertNotNull(request);
        assertEquals(1500, request.getAmount());
        assertEquals("https://mysite.com/payment/return", request.getReturnUrl());
        assertEquals("RUB", request.getCurrency());
        assertEquals(CLIENT_ID, request.getClientId());
        assertNotNull(request.getMetadata());
        assertNotNull(request.getMetadata().getOrderId());
    }

    @Test
    void shouldNotQueryClientIdsWhenUserIdAbsent() {
        detailsRequest.setUserId(null);

        Request request = smackPayService.body(detailsRequest);

        assertNull(request.getClientId());
        verifyNoInteractions(clientIds);
    }

    @Test
    void shouldNotSetClientIdWhenNoClientIdAvailable() {
        when(clientIds.findOrAssignClientId("555555555")).thenReturn(Optional.empty());
        detailsRequest.setUserId("555555555");

        Request request = smackPayService.body(detailsRequest);

        assertNull(request.getClientId());
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
                eq(restClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        smackPayService.createOrder(detailsRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestService).request(
                eq(restClient),
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
        protected TestSmackPayOrderCreationService(RestClient restClient,
                                                   SmackPayProperties smackPayProperties,
                                                   SmackPayClientIds clientIds) {
            super(restClient, smackPayProperties, clientIds);
        }
    }
}
