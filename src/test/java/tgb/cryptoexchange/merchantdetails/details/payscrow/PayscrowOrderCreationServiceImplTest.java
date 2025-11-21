package tgb.cryptoexchange.merchantdetails.details.payscrow;

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
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowPropertiesImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayscrowOrderCreationServiceImplTest {

    @Mock
    private PayscrowPropertiesImpl payscrowProperties;

    @InjectMocks
    private PayscrowOrderCreationServiceImpl payscrowOrderCreationService;

    @Test
    void getMerchantShouldReturnPayscrow() {
        assertEquals(Merchant.PAYSCROW, payscrowOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/order/",
                payscrowOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @ValueSource(strings = {
            "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(payscrowProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(9000);
        detailsRequest.setMethod(Method.SBP.name());
        payscrowOrderCreationService.headers(detailsRequest, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(key, headers.getFirst("X-API-Key"))
        );
    }

    @CsvSource(textBlock = """
            5220,BANK_CARD
            2552,SBP
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        Request request = payscrowOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method, request.getPaymentMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getClientOrderId()))
        );
    }

    @CsvSource(textBlock = """
            8ab90c56-4a96-4f01-be9a-170a9e8f9d68,UNPAID,Альфа,79877892387
            c1098ddc-ef6c-48c0-bd27-cd0f08abffa4,COMPLETED,SBER,6666555544443333
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String id, Status status, String methodName, String holderAccount) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setMethodName(methodName);
        response.setHolderAccount(holderAccount);

        Optional<DetailsResponse> detailsResponse = payscrowOrderCreationService.buildResponse(response);
        assertTrue(detailsResponse.isPresent());
        DetailsResponse actual = detailsResponse.get();
        assertAll(
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(methodName + " " + holderAccount, actual.getDetails()),
                () -> assertEquals(Merchant.PAYSCROW, actual.getMerchant())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "No available traders that match order requirements. Please, try again later or change order parameters.",
            "Amount for the chosen payment method doesn't meet limits.",
            "Internal server error"
    })
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoTraderMessageOrAmountError(String messageTest) throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn(messageTest);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertTrue(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotTraderMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Some fatal error");
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(false);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfSuccessTrue() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(true);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoSuccess() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(false);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfConflictAndAmountError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Expectation Failed. Amount for the chosen payment method doesn't " +
                "meet limits. Check payment method limits and try again.");
        WebClientResponseException.Conflict conflict =
                Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertTrue(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfConflictAndNotAmountError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Another not amount error.");
        WebClientResponseException.Conflict conflict =
                Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfConflictAndJsonProcessingWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payscrowOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.Conflict internalServerError =
                Mockito.mock(WebClientResponseException.Conflict.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError() {
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.UnprocessableEntity.class)));
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.BadGateway.class)));
        assertFalse(payscrowOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }

    @CsvSource("""
            ALFA,6ee288d1-5b67-4745-a7eb-da6c2a095621
            PSB,7d06e334-b365-4f25-985f-331cfde833e1
            GAZ_PROM,19feb8f2-fc3e-44f2-87e5-2e983730cbb8
            OZON,28b3b304-5610-402a-bc9f-c85e2d4147d2
            """)
    @ParameterizedTest
    void keyFunctionShouldReturnInHouseKeyIfBankInHouseMethod(Method method, String inHouseKey) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        when(payscrowProperties.inHouseKey()).thenReturn(inHouseKey);
        assertEquals(inHouseKey, payscrowOrderCreationService.keyFunction().apply(detailsRequest));
    }

    @CsvSource(textBlock = """
            6ee288d1-5b67-4745-a7eb-da6c2a095621
            7d06e334-b365-4f25-985f-331cfde833e1
            19feb8f2-fc3e-44f2-87e5-2e983730cbb8
            """)
    @ParameterizedTest
    void keyFunctionShouldReturnWhiteTriangleKeyIfTriangleMethod(String whiteTriangleKey) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(Method.TRIANGLE.name());
        when(payscrowProperties.whiteTriangleKey()).thenReturn(whiteTriangleKey);
        assertEquals(whiteTriangleKey, payscrowOrderCreationService.keyFunction().apply(detailsRequest));
    }

    @CsvSource(textBlock = """
            TRANS_SBP,6ee288d1-5b67-4745-a7eb-da6c2a095621,1
            BANK_CARD,7d06e334-b365-4f25-985f-331cfde833e1,9999
            SBP,19feb8f2-fc3e-44f2-87e5-2e983730cbb8,5015
            """)
    @ParameterizedTest
    void keyFunctionShouldReturnKeyIfNotTriangleAndNotInHouseMethodsAndAmountLessThan10000(Method method, String key, Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        detailsRequest.setAmount(amount);
        when(payscrowProperties.key()).thenReturn(key);
        assertEquals(key, payscrowOrderCreationService.keyFunction().apply(detailsRequest));
    }

    @CsvSource(textBlock = """
            TRANS_SBP,6ee288d1-5b67-4745-a7eb-da6c2a095621,10000
            BANK_CARD,7d06e334-b365-4f25-985f-331cfde833e1,10001
            SBP,19feb8f2-fc3e-44f2-87e5-2e983730cbb8,25004
            """)
    @ParameterizedTest
    void keyFunctionShouldReturnHighCheckKeyIfNotTriangleAndNotInHouseMethodsAndAmountEqualOrMoreThan10000(Method method, String key, Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        detailsRequest.setAmount(amount);
        when(payscrowProperties.highCheckKey()).thenReturn(key);
        assertEquals(key, payscrowOrderCreationService.keyFunction().apply(detailsRequest));
    }

    @ValueSource(strings = {
            "SBP", "BANK_CARD", "TRANS_SBP"
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnTrueIfMethodIsNotTriangle(Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(method.name());
        assertTrue(payscrowOrderCreationService.isValidRequestPredicate().test(detailsRequest));
    }

    @ValueSource(ints = {
            1, 5235, 9999
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnTrueIfMethodIsTriangleAndAmountLessThan10000(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(Method.TRIANGLE.name());
        detailsRequest.setAmount(amount);
        assertTrue(payscrowOrderCreationService.isValidRequestPredicate().test(detailsRequest));
    }

    @ValueSource(ints = {
            10000, 10001, 962636
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnFalseIfMethodIsTriangleAndAmountEqualOrMoreThan10000(Integer amount) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethod(Method.TRIANGLE.name());
        detailsRequest.setAmount(amount);
        assertFalse(payscrowOrderCreationService.isValidRequestPredicate().test(detailsRequest));
    }
}