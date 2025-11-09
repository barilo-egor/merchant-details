package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayOrderCreationServiceTest {

    @Mock
    private ExtasyPayProperties extasyPayProperties;

    @InjectMocks
    private ExtasyPayOrderCreationService extasyPayOrderCreationService;

    @Test
    void getMerchantShouldReturnExtasyPay() {
        assertEquals(Merchant.EXTASY_PAY, extasyPayOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(), extasyPayOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(extasyPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        extasyPayOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @ValueSource(ints = {
            1211, 5004, 522
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(int amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);

        Request actual = extasyPayOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId()))
        );
    }

    @CsvSource({
            "144008,1234123412341234,PAID,Статус Банк",
            "1533,9876543212345678,OVERPAID,ALFA"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseObjectWithCardNumber(Long id, String requisiteString, Status status, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setCardNumber(requisiteString);
        response.setBankName(bank);

        Optional<DetailsResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(bank + " " + requisiteString, detailsResponse.getDetails()),
                () -> assertEquals(status.name(), detailsResponse.getMerchantOrderStatus())
        );
    }

    @CsvSource({
            "1234123412341234",
            "9876543212345678"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseObjectWithPhoneNumber(String requisiteString) {
        Response response = new Response();
        response.setId(1L);
        response.setStatus(Status.ERROR);
        response.setPhoneNumber(requisiteString);
        response.setBankName("bank");

        Optional<DetailsResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals("bank" + " " + requisiteString, detailsResponse.getDetails())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasCode1AndMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asText()).thenReturn("Unable to get requisites.");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionNotInternalServerError() {
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(WebClientResponseException.BadRequest.class)
        ));
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(WebClientResponseException.Conflict.class)
        ));
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(RuntimeException.class)
        ));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotUnableToGetRequisites() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asText()).thenReturn("AnotherError");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(false);
        when(responseNode.get("code")).thenReturn(codeNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNot1() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(2);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoCode() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(false);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }
}