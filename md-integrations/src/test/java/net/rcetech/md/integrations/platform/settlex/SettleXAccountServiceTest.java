package net.rcetech.md.integrations.platform.settlex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.SettleXProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов settlex без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт settlex попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class SettleXAccountServiceTest {

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SettleXProperties settleXProperties;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/transactions/in", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithKeyArgs")
    void headersShouldAddRequiredHeaders(Merchant merchant, String key) {
        when(settleXProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(key, headers.getFirst("x-merchant-api-key")),
                () -> assertEquals("application/json", headers.getFirst("Content-Type"))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldBuildRequestObject(Merchant merchant, Integer amount, Method method, String gatewayUrl,
                                      String secret, String sbpId, String c2cId) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        if (Method.SBP.equals(method)) {
            when(settleXProperties.sbpId()).thenReturn(sbpId);
        } else {
            when(settleXProperties.c2cId()).thenReturn(c2cId);
        }
        Request actual = service(merchant).body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> {
                    if (Method.SBP.equals(method)) {
                        assertEquals(sbpId, actual.getMethod());
                    } else {
                        assertEquals(c2cId, actual.getMethod());
                    }
                },
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=" + merchant.name()
                        + "&secret=" + secret, actual.getCallbackUri()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getOrderId())),
                () -> assertTrue(actual.getExpiredAt().isBefore(LocalDateTime.now().plusMinutes(20)))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithResponseArgs")
    void buildResponseShouldBuildResponseObject(Merchant merchant, String bankName, String cardName, String id,
                                                String orderId, Status status) {
        Response response = new Response();
        response.setId(id);
        response.setOrderId(orderId);
        response.setStatus(status);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName(bankName);
        requisites.setCardNumber(cardName);
        response.setRequisites(requisites);
        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(cardName, actual.getDetails()),
                () -> assertEquals(bankName, actual.getBank()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(orderId, actual.getMerchantCustomId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(merchant, actual.getMerchant())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoRequisiteError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        SettleXOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asString()).thenReturn("NO_REQUISITE");
        HttpClientErrorException.Conflict conflict = Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertTrue(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotNoRequisiteError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        SettleXOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asString()).thenReturn("ANOTHER_ERROR");
        HttpClientErrorException.Conflict conflict = Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        SettleXOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(false);
        HttpClientErrorException.Conflict conflict = Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"SETTLE_X", "SETTLE_X_15"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        SettleXOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        HttpClientErrorException.Conflict conflict = Mockito.mock(HttpClientErrorException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotConflictException() {
        SettleXOrderCreationService service = service(Merchant.SETTLE_X);
        assertFalse(service.isNoDetailsExceptionPredicate().test(Mockito.mock(HttpClientErrorException.BadRequest.class)));
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpServerErrorException.InternalServerError.class)));
        assertFalse(service.isNoDetailsExceptionPredicate().test(Mockito.mock(RuntimeException.class)));
    }

    static Stream<Merchant> accounts() {
        return Arrays.stream(new String[]{"SETTLE_X", "SETTLE_X_15"}).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithKeyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "JQX1BI3Vs36UnMB"),
                Arguments.of(m, "y701U9erXYfOAdX")
        ));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 5660, Method.SBP, "https://gateway.paysendmmm.online/merchant/settleX",
                        "3UMKcCFZQeFE5uk", "cmhhrccre0shany01q8oah3cd", "cmhhrcwo40szlny01q71c4djw"),
                Arguments.of(m, 12504, Method.C2C, "https://gateway.paysendmmm.online/merchant/settleX",
                        "O9GFCTfz8wf7o2Q", "cmhhrccre0shany01q8oah3cd", "cmhhrcwo40szlny01q71c4djw")
        ));
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "ALFA_BANK", "1234123412341234", "0ab4b1f1-14ee-4403-963d-87f6d8fd0c9b",
                        "bd77984c-14f8-4096-9ab2-730d416d9d81", Status.CREATED),
                Arguments.of(m, "Сбербанк", "79829828282", "033c2af3-d204-4957-aed4-c3f7337b809d",
                        "fc64ac4f-bdb9-4fb6-bbe9-c99671ca3796", Status.IN_PROGRESS)
        ));
    }

    private SettleXOrderCreationService service(Merchant merchant) {
        return new SettleXOrderCreationService(merchant,
                Mockito.mock(org.springframework.web.client.RestClient.class), settleXProperties, callbackConfig);
    }
}
