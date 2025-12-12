package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Request;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Response;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantCallbackEvent;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantOrderCreationServiceTest {

    public static class TestMerchantOrderCreationService extends MerchantOrderCreationService<Response, VoidCallback> {

        protected TestMerchantOrderCreationService(WebClient webClient) {
            super(webClient, Response.class, VoidCallback.class);
        }

        @Override
        public Merchant getMerchant() {
            return Merchant.ALFA_TEAM;
        }

        @Override
        protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
            return uriBuilder -> uriBuilder.path("path").build();
        }

        @Override
        protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
            return httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        }

        @Override
        protected Request body(DetailsRequest detailsRequest) {
            Request request = new Request();
            request.setAmount("1000");
            return request;
        }

        @Override
        protected Optional<DetailsResponse> buildResponse(Response response) {
            DetailsResponse detailsResponse = new DetailsResponse();
            return Optional.of(detailsResponse);
        }
    }

    private ObjectMapper objectMapper;

    private RequestService requestService;

    @Mock
    private KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate;

    @InjectMocks
    private TestMerchantOrderCreationService service;

    @BeforeEach
    void setup() {
        objectMapper = Mockito.mock(ObjectMapper.class);
        service.setObjectMapper(objectMapper);
        requestService = Mockito.mock(RequestService.class);
        service.setRequestService(requestService);
    }

    @Test
    void createOrderShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrownWhileWriteBody() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        DetailsRequest request = new DetailsRequest();
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> service.createOrder(request));
        assertTrue(ex.getMessage().startsWith("Error occurred while mapping body: "));
    }


    @Test
    void createOrderShouldThrowUnavailableExceptionIfWebClientExceptionWasThrown() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenThrow(RuntimeException.class);
        DetailsRequest detailsRequest = new DetailsRequest();
        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createOrder(detailsRequest)
        );
        assertTrue(ex.getMessage().startsWith("Error occurred while creating order: "));
    }

    @Test
    void createOrderShouldThrowUnavailableExceptionIfJsonProcessingExceptionWasThrownWhileReadResponse() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenThrow(JsonProcessingException.class);
        DetailsRequest detailsRequest = new DetailsRequest();
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> service.createOrder(detailsRequest));
        assertTrue(ex.getMessage().startsWith("Error occurred while mapping merchant response: "));
    }

    @Test
    void createOrderShouldThrowUnavailableExceptionIfResponseIsInvalid() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.errorsToString()).thenReturn("");
        when(validationResult.isValid()).thenReturn(false);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        DetailsRequest detailsRequest = new DetailsRequest();
        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createOrder(detailsRequest)
        );
        verify(validationResult).errorsToString();
        assertTrue(ex.getMessage().startsWith("Mapped response is invalid: "));
    }

    @Test
    void createOrderShouldReturnEmptyOptionalIfResponseHasNoDetails() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        when(response.hasDetails()).thenReturn(false);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        DetailsRequest detailsRequest = new DetailsRequest();
        Optional<DetailsResponse> maybeResponse = service.createOrder(detailsRequest);
        assertTrue(maybeResponse.isEmpty());
    }

    @Test
    void createOrderShouldReturnDetailsResponse() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        when(response.hasDetails()).thenReturn(true);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        DetailsRequest detailsRequest = new DetailsRequest();
        Optional<DetailsResponse> maybeResponse = service.createOrder(detailsRequest);
        assertTrue(maybeResponse.isPresent());
    }

    @Test
    void parseMethodShouldThrowMerchantMethodNotFoundExceptionIfNoMethods() {
        DetailsRequest request = new DetailsRequest();
        request.setMethods(new ArrayList<>());
        assertThrows(MerchantMethodNotFoundException.class, () -> service.parseMethod(request, Method.class));
    }

    @Test
    void parseMethodShouldThrowMerchantMethodNotFoundExceptionIfNoMethodsOfPassedMerchant() {
        DetailsRequest request = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method("method").build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method("method").build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method("method").build());
        request.setMethods(methods);
        assertThrows(MerchantMethodNotFoundException.class, () -> service.parseMethod(request, Method.class));
    }

    @ValueSource(strings = {"TO_CARD", "SBP", "CROSS_BORDER"})
    @ParameterizedTest
    void parseMethodShouldReturnMethod(String method) {
        DetailsRequest request = new DetailsRequest();
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(method).build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ONLY_PAYS).method("method").build());
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method("method").build());
        request.setMethods(methods);
        Method actual = service.parseMethod(request, Method.class);
        assertEquals(method, actual.name());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void parseMethodShouldParseFromMethodName(Method method) {
        assertEquals(method, service.parseMethod(method.name(), Method.class));
    }

    @Test
    void parseMethodShouldThrowMerchantMethodNotFoundExceptionForInvalidValue() {
        MerchantMethodNotFoundException ex = assertThrows(
                MerchantMethodNotFoundException.class,
                () -> service.parseMethod("invalid", Method.class)
        );
        assertEquals("Method \"invalid\" for merchant ALFA_TEAM not found.", ex.getMessage());
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalse() {
        assertFalse(service.isNoDetailsExceptionPredicate().test(new RuntimeException()));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalse() {
        assertFalse(service.hasResponseNoDetailsErrorPredicate().test("error"));
    }

    @Test
    void isValidRequestPredicateShouldReturnTrueIfNullDetailsRequest() {
        assertTrue(service.isValidRequestPredicate().test(null));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            method1,1000,53355
            method2,2000,12586
            """)
    void isValidRequestPredicateShouldReturnTrueIfDetailsRequestNotNull(String method, Integer amount, Long id) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(method).build()));
        detailsRequest.setAmount(amount);
        detailsRequest.setId(id);
        assertTrue(service.isValidRequestPredicate().test(detailsRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionIsNull() {
        assertFalse(service.isNoDetailsExceptionPredicate().test(null));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionNotNull() {
        assertFalse(service.isNoDetailsExceptionPredicate().test(new Exception()));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalseIfExceptionIsNull() {
        assertFalse(service.hasResponseNoDetailsErrorPredicate().test(null));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalseIfExceptionNotNull() {
        assertFalse(service.hasResponseNoDetailsErrorPredicate().test("some str"));
    }

    @Test
    void updateStatusShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenThrow(JsonProcessingException.class);
        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void updateStatusShouldThrowServiceUnavailableExceptionIfCallbackHasNoMerchantOrderId() throws JsonProcessingException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void updateStatusShouldThrowServiceUnavailableExceptionIfCallbackHasNoStatus() throws JsonProcessingException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusName()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void updateStatusShouldThrowServiceUnavailableExceptionIfCallbackHasNoStatusDescription() throws JsonProcessingException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusName()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusDescription()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void updateStatusShouldThrowServiceUnavailableExceptionIfExceptionWasThrownWhileSendMessageToTopic() throws JsonProcessingException {
        service.setCallbackKafkaTemplate(callbackKafkaTemplate);
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusName()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusDescription()).thenReturn(Optional.of(""));
        when(callbackKafkaTemplate.send(anyString(), anyString(), any())).thenThrow(RuntimeException.class);
        assertThrows(ServiceUnavailableException.class, () -> service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @CsvSource("""
            merchant-details-bridgeCallback-v1,99f0213a-3828-4dc9-8417-2e22fa140f13,COMPLETED,Завершен
            merchant-details-bridgeCallback-v2,a0af4b95-c0be-426d-8a26-94e13562527f,ERROR,Ошибка
            """)
    @ParameterizedTest
    void updateStatusShouldSendEvent(String topic, String orderId, String status, String statusDescription) throws JsonProcessingException {
        service.setCallbackKafkaTemplate(callbackKafkaTemplate);
        service.callbackTopicName = topic;
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(orderId));
        when(merchantCallback.getStatusName()).thenReturn(Optional.of(status));
        when(merchantCallback.getStatusDescription()).thenReturn(Optional.of(statusDescription));
        ArgumentCaptor<MerchantCallbackEvent> eventCaptor = ArgumentCaptor.forClass(MerchantCallbackEvent.class);
        ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);
        service.updateStatus("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}");
        verify(callbackKafkaTemplate).send(eq(topic), uuidCaptor.capture(), eventCaptor.capture());
        MerchantCallbackEvent actual = eventCaptor.getValue();
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(uuidCaptor.getValue())),
                () -> assertEquals(orderId, actual.getMerchantOrderId()),
                () -> assertEquals(status, actual.getStatus()),
                () -> assertEquals(statusDescription, actual.getStatusDescription()),
                () -> assertEquals(Merchant.ALFA_TEAM, actual.getMerchant())
        );
    }
}