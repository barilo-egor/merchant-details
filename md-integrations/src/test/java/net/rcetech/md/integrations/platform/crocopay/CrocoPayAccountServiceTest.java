package net.rcetech.md.integrations.platform.crocopay;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.CrocoPayProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов crocopay без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт crocopay попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class CrocoPayAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private CrocoPayProperties crocoPayProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void uriBuilderShouldAddUriPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v2/h2h/invoices", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithHeaderArgs")
    void headersShouldAddRequiredHeaders(Merchant merchant, String id, String secret) {
        when(crocoPayProperties.clientId()).thenReturn(id);
        when(crocoPayProperties.clientSecret()).thenReturn(secret);
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(id, Objects.requireNonNull(headers.get("Client-Id")).getFirst()),
                () -> assertEquals(secret, Objects.requireNonNull(headers.get("Client-Secret")).getFirst())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldReturnMappedBody(Merchant merchant, Integer amount, String method, String gatewayUrl,
                                    String secret, Long dealId) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setId(String.valueOf(dealId));
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        Request request = service(merchant).body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(Method.valueOf(method), request.getMethod()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback/" + merchant + "?dealId=" + dealId
                        + "&secret=" + secret, request.getCallbackUrl())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithResponseArgs")
    void buildResponseShouldBuildRequisiteResponseWithAnyRubBank(Merchant merchant, String id, Status status,
                                                                 String requisiteString) {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setId(id);
        transaction.setStatus(status);
        responseData.setTransaction(transaction);
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites(requisiteString);
        paymentRequisites.setPaymentMethod("any_rub_bank");
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);

        Optional<DetailsResponse> maybeRequisiteResponse = service(merchant).buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(requisiteString, actual.getDetails())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBankResponseArgs")
    void buildResponseShouldBuildRequisiteResponseWithNotAnyRubBank(Merchant merchant, String requisiteString,
                                                                    String bank) {
        Response response = new Response();
        Response.ResponseData responseData = new Response.ResponseData();
        Response.ResponseData.Transaction transaction = new Response.ResponseData.Transaction();
        transaction.setId("id");
        transaction.setStatus(Status.DISPUTE);
        responseData.setTransaction(transaction);
        Response.ResponseData.PaymentRequisites paymentRequisites = new Response.ResponseData.PaymentRequisites();
        paymentRequisites.setRequisites(requisiteString);
        paymentRequisites.setPaymentMethod(bank);
        responseData.setPaymentRequisites(paymentRequisites);
        response.setResponseData(responseData);

        Optional<DetailsResponse> maybeRequisiteResponse = service(merchant).buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(requisiteString, actual.getDetails()),
                () -> assertEquals(bank, actual.getBank())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfRequisiteNotFoundCode(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CrocoPayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(response.get("code")).thenReturn(code);
        when(code.asString()).thenReturn("REQUISITE_NOT_FOUND");
        var internalServerError = Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertTrue(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNotRequisiteNotFound(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CrocoPayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(response.get("code")).thenReturn(code);
        when(code.asString()).thenReturn("ANOTHER_ERROR");
        var internalServerError = Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoCode(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CrocoPayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(false);
        var internalServerError = Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CrocoPayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        var internalServerError = Mockito.mock(HttpServerErrorException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError(Merchant merchant) {
        CrocoPayOrderCreationService service = service(merchant);
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpClientErrorException.BadRequest.class)));
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpClientErrorException.Conflict.class)));
        assertFalse(service.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }

    static final String[] ACCOUNTS = {"BASE_51", "BASE_51_HIGH_CHECK", "BASE_51_LOW_CHECK", "BASE_51_SIM", "CROCO_PAY"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithHeaderArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "BTC24MONEY", "JQX1BI3Vs36UnMB"),
                Arguments.of(m, "4aSoUSDBz2", "y701U9erXYfOAdX"),
                Arguments.of(m, "id", "w1vGjx4COVk531JZgj6dsh7uT")));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 2100, "TO_CARD", "https://gateway.paysendmmm.online", "13NFHS8pzxsFwZr", 5634L),
                Arguments.of(m, 2100, "SBP", "https://bulba.paysendmmm.online", "SP9HHlNKw0MIKas", 25535L)));
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "7cc259a2-67a2-4a67-8e7e-e0342c08da81", Status.PENDING, "1234123412341234"),
                Arguments.of(m, "418adf2c-c382-42f4-8bbc-0d0ea008f701", Status.SUCCESS, "9876987654325432")));
    }

    static Stream<Arguments> merchantWithBankResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "1234123412341234", "ALFA"),
                Arguments.of(m, "9876987654325432", "SBER")));
    }

    private CrocoPayOrderCreationService service(Merchant merchant) {
        return new CrocoPayOrderCreationService(merchant, restClient, crocoPayProperties, callbackConfig);
    }
}
