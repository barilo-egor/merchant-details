package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.NeuralPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NeuralPayOrderCreationServiceTest {

    @Mock
    private NeuralPayProperties neuralPayProperties;

    @InjectMocks
    private NeuralPayOrderCreationService service;

    @Mock
    private WebClient webClient;

    @Mock
    private RequestService requestService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/v1/core/transactions/charge",
                service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMB", "y701U9erXYfOAdX", "k531JZgj6dsh7uT"
    })
    @DisplayName("Проверка добавления заголовков")
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        when(neuralPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("accept")).getFirst())
        );
    }

    @ParameterizedTest
    @DisplayName("Создание тела запроса с проверкой реквизитов")
    @CsvSource(textBlock = """
            5000, P2P_CARD
            10500, P2P_PHONE
            """)
    void body(String amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(Integer.valueOf(amount));
        detailsRequest.setMethods(
                List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.NEURAL_PAY).method(method.name())
                        .build()));

        Request actual = service.body(detailsRequest);
        Request.Requisite requisite = new Request.Requisite();
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), actual.getAmount()),
                () -> assertNotNull(actual.getRequisite()),
                () -> assertEquals(requisite.getCurrency(), actual.getRequisite().getCurrency()),
                () -> {
                    List<String> expectedMethods = Collections.singletonList(method.name());
                    assertEquals(expectedMethods, actual.getMethod());
                }
        );
    }

    @ParameterizedTest
    @DisplayName("Создание тела ответа с проверкой реквизитов")
    @CsvSource(textBlock = """
            1000, ID-444, PENDING, Сбербанк, 2200111122223333
            500, ID-555, CHARGED, Альфа-Банк, 4444555566667777
            """)
    void buildResponseShouldBuildResponseObject(String amount, String id, Status status,
            String bankName, String requisiteValue) {
        Response response = new Response();
        response.setAmount(amount);
        response.setId(id);
        response.setStatus(status);

        Response.ResponseRequisite requisite = new Response.ResponseRequisite();
        requisite.setBankName(bankName);
        requisite.setRequisite(requisiteValue);
        response.setRequisite(requisite);

        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);

        assertTrue(maybeResponse.isPresent(), "Ответ должен присутствовать");
        DetailsResponse actual = maybeResponse.get();

        String expectedDetails = String.format("%s %s", bankName, requisiteValue);
        assertAll(
                () -> assertEquals(Integer.valueOf(amount), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(expectedDetails, actual.getDetails()),
                () -> assertEquals(service.getMerchant(), actual.getMerchant())
        );
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45
            578f16e0-5941-4330-80c3-b2d22ef302b8
            """)
    @ParameterizedTest
    @DisplayName("Отмена заказа: проверка формирования корректного пути и JSON тела")
    void makeCancelRequestShouldMakeRequest(String orderId) throws JsonProcessingException {
        service.setRequestService(requestService);
        ObjectMapper objectMapper = new ObjectMapper();
        service.setObjectMapper(objectMapper);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);

        String expectedBody = objectMapper.writeValueAsString(Map.of("transaction_id", orderId));

        service.makeCancelRequest(cancelOrderRequest);

        verify(requestService).request(
                eq(webClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                any(),
                eq(expectedBody)
        );

        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI resultUri = uriBuilderCaptor.getValue().apply(uriBuilder);

        assertEquals("/v1/core/transactions/cancel", resultUri.getPath());
    }

    @Test
    void getMerchantShouldReturnNeuralPay() {
        assertEquals(Merchant.NEURAL_PAY, service.getMerchant());
    }

}