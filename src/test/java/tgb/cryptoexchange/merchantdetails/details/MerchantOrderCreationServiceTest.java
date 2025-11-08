package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Request;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Response;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.MerchantMethodNotFoundException;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantOrderCreationServiceTest {

    public static class TestMerchantOrderCreationService extends MerchantOrderCreationService<Response> {

        protected TestMerchantOrderCreationService(WebClient webClient) {
            super(webClient, Response.class);
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
        ServiceUnavailableException ex = assertThrows(ServiceUnavailableException.class, () -> service.createOrder(null));
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
}