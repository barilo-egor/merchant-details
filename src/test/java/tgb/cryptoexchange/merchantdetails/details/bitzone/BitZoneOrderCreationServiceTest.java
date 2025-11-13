package tgb.cryptoexchange.merchantdetails.details.bitzone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.BitZoneProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitZoneOrderCreationServiceTest {

    @Mock
    private BitZoneProperties bitZoneProperties;

    @InjectMocks
    private BitZoneOrderCreationService bitZoneOrderCreationService;

    @Test
    void getMerchantShouldReturnBitZoneMerchant() {
        assertEquals(Merchant.BIT_ZONE, bitZoneOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/payment/trading/pay-in", bitZoneOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMB", "y701U9erXYfOAdX", "k531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(bitZoneProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        bitZoneOrderCreationService.headers(null, "").accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @CsvSource({
            "2100,CARD",
            "2100,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        Request request = bitZoneOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getFiatAmount()),
                () -> assertEquals(Method.valueOf(method), request.getMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExtra().getExternalTransactionId()))
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,ALFA",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,ACTIVE,9876987654325432,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithCard(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setMethod(Method.CARD);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank(bank);
        requisite.setRequisites(requisiteString);
        response.setRequisite(requisite);

        Optional<DetailsResponse> maybeRequisiteResponse = bitZoneOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.BIT_ZONE, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @CsvSource({
            "79284565465,ALFA",
            "147896541,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithSbp(String requisiteString, String bank) {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.ACTIVE);
        response.setMethod(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        requisite.setBank(bank);
        requisite.setSbpNumber(requisiteString);
        response.setRequisite(requisite);

        Optional<DetailsResponse> maybeRequisiteResponse = bitZoneOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnTrueIfMessagePleaseContactSupport() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.has("message")).thenReturn(true);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get("message")).thenReturn(messageNode);
        when(messageNode.asText()).thenReturn("SBP_METHOD_DISABLED_PLEASE_CONTACT_SUPPORT");
        assertTrue(bitZoneOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalseIfMessageNotPleaseContactSupport() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.has("message")).thenReturn(true);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(jsonNode.get("message")).thenReturn(messageNode);
        when(messageNode.asText()).thenReturn("SOME_ERROR");
        assertFalse(bitZoneOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldReturnFalseIfNoMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        when(jsonNode.has("message")).thenReturn(false);
        assertFalse(bitZoneOrderCreationService.hasResponseNoDetailsErrorPredicate().test(""));
    }

    @Test
    void hasResponseNoDetailsErrorPredicateShouldThrowServiceUnavailableExceptionIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        Predicate<String> hasResponseNoDetailsErrorPredicate = bitZoneOrderCreationService.hasResponseNoDetailsErrorPredicate();
        assertThrows(ServiceUnavailableException.class, () -> hasResponseNoDetailsErrorPredicate.test(""));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("CANT_CREATE_TRADE_FOR_THIS_AMOUNT");
        WebClientResponseException.Forbidden forbidden = Mockito.mock(WebClientResponseException.Forbidden.class);
        when(forbidden.getResponseBodyAsString()).thenReturn("");
        assertTrue(bitZoneOrderCreationService.isNoDetailsExceptionPredicate().test(forbidden));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfSbpDisabled() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("SBP_METHOD_DISABLED_PLEASE_CONTACT_SUPPORT");
        WebClientResponseException.Forbidden forbidden = Mockito.mock(WebClientResponseException.Forbidden.class);
        when(forbidden.getResponseBodyAsString()).thenReturn("");
        assertTrue(bitZoneOrderCreationService.isNoDetailsExceptionPredicate().test(forbidden));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(false);
        WebClientResponseException.Forbidden forbidden = Mockito.mock(WebClientResponseException.Forbidden.class);
        when(forbidden.getResponseBodyAsString()).thenReturn("");
        assertFalse(bitZoneOrderCreationService.isNoDetailsExceptionPredicate().test(forbidden));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        bitZoneOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.Forbidden forbidden = Mockito.mock(WebClientResponseException.Forbidden.class);
        when(forbidden.getResponseBodyAsString()).thenReturn("");
        assertFalse(bitZoneOrderCreationService.isNoDetailsExceptionPredicate().test(forbidden));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfGatewayTimeout() {
        assertTrue(bitZoneOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.GatewayTimeout.class)));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotForbidden() {
        assertFalse(bitZoneOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.InternalServerError.class)));
        assertFalse(bitZoneOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.BadRequest.class)));
        assertFalse(bitZoneOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }
}