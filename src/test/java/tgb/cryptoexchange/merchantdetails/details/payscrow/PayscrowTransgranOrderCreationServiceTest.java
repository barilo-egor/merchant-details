package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PayscrowTransgranOrderCreationServiceTest {

    @InjectMocks
    private PayscrowTransgranOrderCreationService service;

    private static final String BASE_URL = "https://exchange.test";
    @Mock
    private CallbackConfig callbackConfig;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RequestService requestService;
    @Mock
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "https://exchange.test");

        service.setRequestService(requestService);
        service.setObjectMapper(objectMapper);
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/form/create",
                service.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @CsvSource(textBlock = """
            5000,https://t.me/bot_url_1
            15000,https://t.me/bot_url_2
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, String redirectUrl) {

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setRedirectUrl(redirectUrl);

        Request request = service.body(detailsRequest);

        assertNotNull(request);
        assertNotNull(request.getOrderData());

        String clientOrderId = request.getOrderData().getClientOrderId();
        assertAll(
                () -> assertEquals(amount, request.getOrderData().getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(clientOrderId)),
                () -> {
                    assertEquals(redirectUrl, request.getRedirectUrl());
                    assertEquals(redirectUrl, request.getReturnUrl());
                }
        );
    }

    @CsvSource(textBlock = """
            order-id-123,https://pay.payscrow.io/form/1,5430.00
            order-id-456,https://pay.payscrow.io/form/2,10000.50
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String orderId, String formUrl, Double amount) {
        ResponseTransgran response = new ResponseTransgran();
        response.setSuccess(true);

        ResponseTransgran.Data data = new ResponseTransgran.Data();
        ResponseTransgran.Data.OrderData orderData = new ResponseTransgran.Data.OrderData();
        orderData.setOrderId(orderId);
        orderData.setAmount(amount);

        data.setOrderData(orderData);
        data.setFormUrl(formUrl);
        data.setStatus(Status.UNPAID);
        response.setData(data);

        Optional<DetailsResponse> detailsResponse = service.buildResponse(response);

        assertTrue(detailsResponse.isPresent());
        DetailsResponse actual = detailsResponse.get();
        assertAll(
                () -> assertEquals(orderId, actual.getMerchantOrderId()),
                () -> assertEquals(formUrl, actual.getDetails()),
                () -> assertEquals(Merchant.PAYSCROW_TRANSGRAN, actual.getMerchant()),
                () -> assertEquals(amount.intValue(), actual.getAmount())
        );
    }

    @Test
    void getMerchantShouldReturnPayscrowLow() {
        assertEquals(Merchant.PAYSCROW_TRANSGRAN, service.getMerchant());
    }
}