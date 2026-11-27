package net.rcetech.md.integrations.platform.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PayscrowProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов payscrow без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт payscrow попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class PayscrowAccountServiceTest {

    static final String[] ACCOUNTS = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW", "PAYSCROW_SIM",
            "PAYSCROW_WHITE_TRIANGLE"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    @Mock
    private PayscrowProperties payscrowProperties;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/order/",
                service(merchant).uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithKeyArgs")
    void headersShouldAddRequiredHeaders(Merchant merchant, String key) {
        when(payscrowProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(9000);
        service(merchant).headers(detailsRequest, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(key, headers.getFirst("X-API-Key"))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldBuildRequestObject(Merchant merchant, Integer amount, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        Request request = service(merchant).body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method, request.getPaymentMethod()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getClientOrderId())),
                () -> assertEquals(Merchant.PAYSCROW.equals(merchant), Boolean.TRUE.equals(request.getUniqueAmount()))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithResponseArgs")
    void buildResponseShouldBuildResponseObject(Merchant merchant, String id, Status status, String methodName,
                                                String holderAccount, Double amount) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setMethodName(methodName);
        response.setHolderAccount(holderAccount);
        response.setAmount(amount);

        Optional<DetailsResponse> detailsResponse = service(merchant).buildResponse(response);
        assertTrue(detailsResponse.isPresent());
        DetailsResponse actual = detailsResponse.get();
        assertAll(
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(holderAccount, actual.getDetails()),
                () -> assertEquals(methodName, actual.getBank()),
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals(amount.intValue(), actual.getAmount())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void buildResponseShouldBuildResponseObjectWithoutAmountIfNoAmount(Merchant merchant) {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.CANCELED_BY_SERVICE);
        response.setMethodName(Method.ALFA.name());
        response.setHolderAccount("holderAccount");

        Optional<DetailsResponse> detailsResponse = service(merchant).buildResponse(response);
        assertTrue(detailsResponse.isPresent());
        DetailsResponse actual = detailsResponse.get();
        assertNull(actual.getAmount());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoTraderMessageOrAmountError(Merchant merchant)
            throws JacksonException {
        List<String> messages = List.of(
                "No available traders that match order requirements. Please, try again later or change order parameters.",
                "Amount for the chosen payment method doesn't meet limits.",
                "Internal server error"
        );
        for (String messageTest : messages) {
            ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
            PayscrowOrderCreationService service = service(merchant);
            service.setObjectMapper(objectMapper);
            JsonNode response = Mockito.mock(JsonNode.class);
            when(objectMapper.readTree(anyString())).thenReturn(response);
            when(response.has("success")).thenReturn(true);
            JsonNode success = Mockito.mock(JsonNode.class);
            when(response.get("success")).thenReturn(success);
            when(success.asBoolean()).thenReturn(false);
            when(response.has("message")).thenReturn(true);
            JsonNode message = Mockito.mock(JsonNode.class);
            when(response.get("message")).thenReturn(message);
            when(message.asString()).thenReturn(messageTest);
            HttpServerErrorException.InternalServerError internalServerError =
                    Mockito.mock(HttpServerErrorException.InternalServerError.class);
            when(internalServerError.getResponseBodyAsString()).thenReturn("");
            assertTrue(service.isNoDetailsExceptionPredicate().test(internalServerError));
        }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotTraderMessage(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Some fatal error");
        HttpServerErrorException.InternalServerError internalServerError =
                Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(false);
        HttpServerErrorException.InternalServerError internalServerError =
                Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfSuccessTrue(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(true);
        HttpServerErrorException.InternalServerError internalServerError =
                Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoSuccess(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(false);
        HttpServerErrorException.InternalServerError internalServerError =
                Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        HttpServerErrorException.InternalServerError internalServerError =
                Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfConflictAndAmountError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Expectation Failed. Amount for the chosen payment method doesn't " +
                "meet limits. Check payment method limits and try again.");
        HttpClientErrorException.Conflict conflict =
                Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertTrue(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfConflictAndNotAmountError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("success")).thenReturn(true);
        JsonNode success = Mockito.mock(JsonNode.class);
        when(response.get("success")).thenReturn(success);
        when(success.asBoolean()).thenReturn(false);
        when(response.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Another not amount error.");
        HttpClientErrorException.Conflict conflict =
                Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfConflictAndJsonProcessingWasThrown(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        PayscrowOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        HttpClientErrorException.Conflict conflict =
                Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW", "PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError(Merchant merchant) {
        PayscrowOrderCreationService service = service(merchant);
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpClientErrorException.UnprocessableEntity.class)));
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpServerErrorException.BadGateway.class)));
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }

    @ValueSource(strings = {
            "SBP", "BANK_CARD", "TRANS_SBP"
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnTrueIfMethodIsNotTriangle(Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        assertTrue(service(Merchant.PAYSCROW).isValidRequestPredicate(method.name()).test(detailsRequest));
    }

    @ValueSource(ints = {
            1, 5235, 9999
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnTrueIfMethodIsTriangleAndAmountLessThan10000(Integer amount) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        assertTrue(service(Merchant.PAYSCROW).isValidRequestPredicate(Method.TRIANGLE.name()).test(detailsRequest));
    }

    @ValueSource(ints = {
            10000, 10001, 962636
    })
    @ParameterizedTest
    void isValidRequestPredicateShouldReturnFalseIfMethodIsTriangleAndAmountEqualOrMoreThan10000(Integer amount) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        assertFalse(service(Merchant.PAYSCROW).isValidRequestPredicate(Method.TRIANGLE.name()).test(detailsRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAYSCROW_HIGH_CHECK", "PAYSCROW_LOW",
            "PAYSCROW_SIM", "PAYSCROW_WHITE_TRIANGLE"})
    void isValidRequestPredicateShouldAllowTriangleOfAnyAmountForOtherAccounts(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(962636);
        assertTrue(service(merchant).isValidRequestPredicate(Method.TRIANGLE.name()).test(detailsRequest));
    }

    static Stream<Arguments> merchantWithKeyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "PzeiDYNh1RTRD5d"),
                Arguments.of(m, "L76sF2r7uL1ClNF")
        ));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 5220, Method.BANK_CARD),
                Arguments.of(m, 2552, Method.SBP)
        ));
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "8ab90c56-4a96-4f01-be9a-170a9e8f9d68", Status.UNPAID, "Альфа", "79877892387", 5430.26),
                Arguments.of(m, "c1098ddc-ef6c-48c0-bd27-cd0f08abffa4", Status.COMPLETED, "SBER", "6666555544443333", 5360.00)
        ));
    }

    private PayscrowOrderCreationService service(Merchant merchant) {
        return new PayscrowOrderCreationService(merchant, Mockito.mock(org.springframework.web.client.RestClient.class),
                payscrowProperties);
    }
}
