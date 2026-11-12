package tgb.cryptoexchange.merchantdetails.details;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Request;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Response;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantOrderCreationServiceTest {

    @Test
    void createOrderShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrownWhileWriteBody() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenThrow(JacksonException.class);
        OrderCreationRequest request = new OrderCreationRequest();
        request.setMethod("CARD");

        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> service.createOrder(request));
        assertTrue(ex.getMessage().startsWith("Error occurred while mapping body: "));
    }

    private ObjectMapper objectMapper;

    private RequestService requestService;

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
    void createOrderShouldThrowUnavailableExceptionIfRestClientExceptionWasThrown() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenThrow(RuntimeException.class);
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createOrder(detailsRequest)
        );
        assertTrue(ex.getMessage().startsWith("Error occurred while creating order: "));
    }

    @Test
    void createOrderShouldReturnEmptyIfResponseTimeoutExceeded() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString()))
                .thenThrow(new ResourceAccessException("timeout", new HttpTimeoutException("request timed out")));
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        assertTrue(service.createOrder(detailsRequest).isEmpty());
    }

    @Test
    void createOrderShouldThrowUnavailableExceptionIfConnectTimeoutExceeded() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString()))
                .thenThrow(new ResourceAccessException("timeout", new HttpConnectTimeoutException("connect timed out")));
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createOrder(detailsRequest)
        );
        assertTrue(ex.getMessage().startsWith("Error occurred while creating order: "));
    }

    @Test
    void createOrderShouldThrowUnavailableExceptionIfJsonProcessingExceptionWasThrownWhileReadResponse() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenThrow(JacksonException.class);
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> service.createOrder(detailsRequest));
        assertTrue(ex.getMessage().startsWith("Error occurred while mapping merchant response: "));
    }

    @Test
    void createOrderShouldThrowUnavailableExceptionIfResponseIsInvalid() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.errorsToString()).thenReturn("");
        when(validationResult.isValid()).thenReturn(false);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createOrder(detailsRequest)
        );
        verify(validationResult).errorsToString();
        assertTrue(ex.getMessage().startsWith("Mapped response is invalid: "));
    }

    @Test
    void createOrderShouldReturnEmptyOptionalIfResponseHasNoDetails() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        when(response.hasDetails()).thenReturn(false);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        Optional<DetailsResponse> maybeResponse = service.createOrder(detailsRequest);
        assertTrue(maybeResponse.isEmpty());
    }

    @Test
    void createOrderShouldReturnDetailsResponse() throws JacksonException {
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = Mockito.mock(Response.class);
        ValidationResult validationResult = Mockito.mock(ValidationResult.class);
        when(response.validate()).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        when(response.hasDetails()).thenReturn(true);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<Class<Object>>any())).thenReturn(response);
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod("CARD");
        detailsRequest.setId(String.valueOf(5234));
        detailsRequest.setUserId(String.valueOf(3745747545L));
        Optional<DetailsResponse> maybeResponse = service.createOrder(detailsRequest);
        assertTrue(maybeResponse.isPresent());
    }

    @Test
    void parseMethodShouldThrowMerchantMethodNotFoundExceptionIfMethodNotExists() {
        assertThrows(MerchantMethodNotFoundException.class, () -> service.parseMethod("CARD", Method.class));
    }

    @Test
    void isValidRequestPredicateShouldReturnTrueIfNullDetailsRequest() {
        assertTrue(service.isValidRequestPredicate("ALFA").test(null));
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

    @ParameterizedTest
    @CsvSource(textBlock = """
            method1,1000,53355
            method2,2000,12586
            """)
    void isValidRequestPredicateShouldReturnTrueIfDetailsRequestNotNull(String method, Integer amount, String id) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(method);
        detailsRequest.setAmount(amount);
        detailsRequest.setId(id);
        assertTrue(service.isValidRequestPredicate("ALFA").test(detailsRequest));
    }

    public static class TestMerchantOrderCreationService extends MerchantOrderCreationService<Response, VoidCallback> {

        protected TestMerchantOrderCreationService(RestClient restClient) {
            super(restClient, Response.class, VoidCallback.class);
        }

        @Override
        public Merchant getMerchant() {
            return Merchant.ALFA_TEAM;
        }

        @Override
        protected Function<UriBuilder, URI> uriBuilder(OrderCreationRequest request) {
            return uriBuilder -> uriBuilder.path("path").build();
        }

        @Override
        protected Consumer<HttpHeaders> headers(OrderCreationRequest request, String body) {
            return httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        }

        @Override
        protected Request body(OrderCreationRequest creationRequest) {
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
    void parseCallbackShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrown() throws JacksonException {
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenThrow(JacksonException.class);
        assertThrows(ServiceUnavailableException.class, () -> service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void parseCallbackShouldThrowServiceUnavailableExceptionIfCallbackHasNoMerchantOrderId() throws JacksonException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void parseCallbackShouldThrowServiceUnavailableExceptionIfCallbackHasNoStatus() throws JacksonException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusName()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void parseCallbackShouldThrowServiceUnavailableExceptionIfCallbackHasNoStatusDescription() throws JacksonException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusName()).thenReturn(Optional.of(""));
        when(merchantCallback.getStatusDescription()).thenReturn(Optional.empty());
        assertThrows(ServiceUnavailableException.class, () -> service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @Test
    void parseCallbackShouldReturnStatusOfCallback() throws JacksonException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.getMerchantOrderId()).thenReturn(Optional.of("99f0213a-3828-4dc9-8417-2e22fa140f13"));
        when(merchantCallback.getStatusName()).thenReturn(Optional.of("COMPLETED"));
        when(merchantCallback.getStatusDescription()).thenReturn(Optional.of("Завершен"));

        assertEquals(Optional.of(new CallbackStatus("99f0213a-3828-4dc9-8417-2e22fa140f13", "COMPLETED", "Завершен")),
                service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "{}"})
    void parseCallbackShouldReturnEmptyIfBodyIsEmpty(String body) {
        assertEquals(Optional.empty(), service.parseCallback(body));
        Mockito.verifyNoInteractions(objectMapper);
    }

    @Test
    void isIgnoreCallbackPredicateShouldReturnFalseByDefault() {
        VoidCallback callback = new VoidCallback();
        assertFalse(service.isIgnoreCallbackPredicate().test(callback));
    }

    @Test
    void parseCallbackShouldReturnEmptyIfCallbackIsIgnored() throws JacksonException {
        VoidCallback merchantCallback = Mockito.mock(VoidCallback.class);
        when(objectMapper.readValue(anyString(), eq(VoidCallback.class))).thenReturn(merchantCallback);
        when(merchantCallback.isIgnored()).thenReturn(true);

        assertEquals(Optional.empty(), service.parseCallback("{\"someField\":\"someValue\",\"merchant\":\"ALFA_TEAM\"}"));
    }
}
