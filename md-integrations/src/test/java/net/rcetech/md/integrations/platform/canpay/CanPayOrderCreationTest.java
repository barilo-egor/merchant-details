package net.rcetech.md.integrations.platform.canpay;

import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.CanPayProperties;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanPayOrderCreationTest {

    private static final String API_KEY = "test-api-key";
    private static final String PHONE = "+79991234567";
    private static final String USER_ID = "123456789";
    private static final String CLIENT_ID = "987654321";

    private final ObjectMapper objectMapper = JsonMappers.create();

    @Mock
    private RestClient restClient;

    @Mock
    private CanPayProperties canPayProperties;

    @Mock
    private RequestService requestService;

    @Mock
    private CanPayClientIds clientIds;

    private CanPayOrderCreationService canPayService;

    private OrderCreationRequest detailsRequest;

    @BeforeEach
    void setUp() {
        canPayService = new TestCanPayOrderCreationService(
                restClient,
                canPayProperties,
                clientIds
        );
        canPayService.setObjectMapper(objectMapper);
        canPayService.setRequestService(requestService);

        detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(Method.QR.name());
        detailsRequest.setAmount(1500);
        detailsRequest.setPhone(PHONE);
        detailsRequest.setRedirectUrl("https://mysite.com/payment/return");
        detailsRequest.setUserId(USER_ID);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        String responseJson = """
                {
                  "id": "canpay-order-777",
                  "amount": 1500,
                  "status": "pending",
                  "payment_url": "https://example.com/checkout/canpay-order-777"
                }
                """;

        when(requestService.request(
                eq(restClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        Optional<DetailsResponse> result = canPayService.createOrder(detailsRequest);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();
        assertThat(detailsResponse.getMerchant()).isEqualTo(Merchant.CANPAY);
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("canpay-order-777");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
        assertThat(detailsResponse.getDetails()).isEqualTo("https://example.com/checkout/canpay-order-777");
        assertThat(detailsResponse.getAmount()).isEqualTo(1500);
    }

    @Test
    void shouldBuildCorrectRequestBody() {
        when(clientIds.findOrAssignClientId(USER_ID)).thenReturn(Optional.of(CLIENT_ID));

        Request request = canPayService.body(detailsRequest);

        assertNotNull(request);
        assertEquals(1500, request.getAmount());
        assertEquals(PHONE, request.getPhone());
        assertEquals("https://mysite.com/payment/return", request.getReturnUrl());
        assertEquals(CLIENT_ID, request.getClientId());
        assertNotNull(request.getMetadata());
        assertNotNull(request.getMetadata().getOrderId());
    }

    @Test
    void shouldNotQueryClientIdsWhenUserIdAbsent() {
        detailsRequest.setUserId(null);

        Request request = canPayService.body(detailsRequest);

        assertNull(request.getClientId());
        verifyNoInteractions(clientIds);
    }

    @Test
    void shouldNotSetClientIdWhenNoClientIdAvailable() {
        when(clientIds.findOrAssignClientId("555555555")).thenReturn(Optional.empty());
        detailsRequest.setUserId("555555555");

        Request request = canPayService.body(detailsRequest);

        assertNull(request.getClientId());
    }

    @Test
    void shouldSetCorrectHeaders() {
        when(canPayProperties.apiKey()).thenReturn(API_KEY);

        String responseJson = """
                {
                  "id": "canpay-order-777",
                  "amount": 1500,
                  "status": "pending",
                  "payment_url": "https://example.com/checkout/canpay-order-777"
                }
                """;

        when(requestService.request(
                eq(restClient),
                eq(HttpMethod.POST),
                any(),
                any(),
                anyString()
        )).thenReturn(responseJson);

        canPayService.createOrder(detailsRequest);

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
    void getMerchantShouldReturnCanPay() {
        assertEquals(Merchant.CANPAY, canPayService.getMerchant());
    }

    private static class TestCanPayOrderCreationService extends CanPayOrderCreationService {
        protected TestCanPayOrderCreationService(RestClient restClient,
                                                 CanPayProperties canPayProperties,
                                                 CanPayClientIds clientIds) {
            super(restClient, canPayProperties, clientIds);
        }
    }
}
