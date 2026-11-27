package net.rcetech.md.integrations.platform.paylee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PayLeeProperties;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Общий тест аккаунтов paylee без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт paylee попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class PayLeeAccountServiceTest {

    @Mock
    private PayLeeProperties payLeeProperties;

    @Mock
    private RestClient restClient;

    static final String[] ACCOUNTS = {"PAY_LEE", "PAY_LEE_QR"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private PayLeeMerchantService service(Merchant merchant) {
        return new PayLeeMerchantService(merchant, restClient, payLeeProperties);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/partners/purchases/", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @MethodSource("merchantWithTokenArgs")
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(Merchant merchant, String token) {
        HttpHeaders headers = new HttpHeaders();
        when(payLeeProperties.token()).thenReturn(token);
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Token " + token, headers.getFirst(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE))
        );
    }

    @MethodSource("merchantWithBodyArgs")
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Merchant merchant, Integer amount, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        detailsRequest.setUserId(String.valueOf(1231231231L));
        Request actual = service(merchant).body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getPrice()),
                () -> assertEquals(method, actual.getRequisitesType()),
                () -> assertNotNull(actual.getClientId()),
                () -> assertEquals(8, actual.getClientId().length()),
                () -> assertEquals("USDT", actual.getAmountCurrency())
        );
    }

    @MethodSource("merchantWithResponseArgs")
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(Merchant merchant, String bankName, String requisites, Integer id,
                                                Status status, Double price) {
        Response response = new Response();
        response.setBankName(bankName);
        response.setRequisites(requisites);
        response.setId(id);
        response.setStatus(status);
        response.setPrice(price);

        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bankName, actual.getBank()),
                () -> assertEquals(requisites, actual.getDetails()),
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(price.intValue(), actual.getAmount())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void buildResponseShouldSetRequisitesIfHasNotQRMethod(Merchant merchant) {
        Response response = new Response();
        response.setBankName("bankName");
        response.setRequisites("requisites");
        response.setId(1);
        response.setStatus(Status.PENDING);
        response.setPrice(1000.0);
        response.setRequisitesType(List.of(Method.CARD));
        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals("bankName", actual.getBank());
        assertEquals("requisites", actual.getDetails());
        assertNull(actual.getQr());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void buildResponseShouldSetQRIfHasQRMethod(Merchant merchant) {
        Response response = new Response();
        response.setBankName("bankName");
        response.setRequisites("requisites");
        response.setId(1);
        response.setStatus(Status.PENDING);
        response.setPrice(1000.0);
        response.setRequisitesType(List.of(Method.ANY_QR));
        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals("requisites", actual.getQr());
        assertNull(actual.getBank());
        assertNull(actual.getDetails());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoTraderError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(1);
        JsonNode errorNode = Mockito.mock(JsonNode.class);
        when(arrayNode.get(0)).thenReturn(errorNode);
        when(errorNode.asString()).thenReturn("Нет доступного трейдера для вашего запроса. Попробуйте повторить позже.");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotNoTraderError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(1);
        JsonNode errorNode = Mockito.mock(JsonNode.class);
        when(arrayNode.get(0)).thenReturn(errorNode);
        when(errorNode.asString()).thenReturn("Неправильный запрос.");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArrayIsEmpty(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(0);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @MethodSource("merchantWithSizeArgs")
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArraySizeMoreThan1(Merchant merchant, int size)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(size);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNonFieldErrorsIsNotArray(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoNonFieldErrors(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(Mockito.anyString())).thenThrow(JacksonException.class);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionIsNotBadRequest(Merchant merchant) {
        assertFalse(service(merchant).isNoDetailsExceptionPredicate().test(Mockito.mock(HttpClientErrorException.Conflict.class)));
        assertFalse(service(merchant).isNoDetailsExceptionPredicate().test(Mockito.mock(HttpServerErrorException.InternalServerError.class)));
        assertFalse(service(merchant).isNoDetailsExceptionPredicate().test(Mockito.mock(RuntimeException.class)));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(true);
        when(price.size()).thenReturn(1);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(price.get(0)).thenReturn(messageNode);
        when(messageNode.asString()).thenReturn("Убедитесь, что это значение больше 1000");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotAmountError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(true);
        when(price.size()).thenReturn(1);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(price.get(0)).thenReturn(messageNode);
        when(messageNode.asString()).thenReturn("Другая ошибка");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @MethodSource("merchantWithSizeArgs")
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceSizeNot1(Merchant merchant, int size)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(true);
        when(price.size()).thenReturn(size);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceIsNotArray(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"PAY_LEE", "PAY_LEE_QR"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoPrice(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("nonFieldErrors")).thenReturn(false);
        when(response.has("price")).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @MethodSource("merchantWithOrderIdArgs")
    @ParameterizedTest
    void sendReceiptShouldCallRequestServiceMethod(Merchant merchant, String orderId) {
        RequestService requestService = Mockito.mock(RequestService.class);
        PayLeeMerchantService payLeeService = service(merchant);
        payLeeService.setRequestService(requestService);
        byte[] fileContent = "test content".getBytes();
        String fileName = "receipt.jpg";
        when(payLeeProperties.token()).thenReturn("token");
        payLeeService.sendReceipt(orderId, fileContent, fileName);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor = ArgumentCaptor.forClass(Function.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestService).requestAsync(
                eq(restClient),
                eq(HttpMethod.POST),
                uriBuilderCaptor.capture(),
                headersCaptor.capture(),
                any(MultiValueMap.class),
                any()
        );
        URI uri = uriBuilderCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertAll(
                () -> assertTrue(uri.getPath().endsWith("/partners/purchases/" + orderId + "/receipt")),
                () -> assertEquals("Token token", headers.getFirst("Authorization")),
                () -> assertEquals("multipart/form-data", headers.getFirst("Content-Type"))
        );
    }

    static Stream<Arguments> merchantWithTokenArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "PzeiDYNh1RTRD5d"),
                Arguments.of(m, "L76sF2r7uL1ClNF")
        ));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 5600, Method.CARD),
                Arguments.of(m, 2504, Method.SBP)
        ));
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "Альфа банк", "1234123412341234", 55003, Status.PENDING, 55004.0),
                Arguments.of(m, "Тинькофф", "79869869898", 2400, Status.COMPLETED, 2399.0)
        ));
    }

    static Stream<Arguments> merchantWithSizeArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 2),
                Arguments.of(m, 10),
                Arguments.of(m, 100)
        ));
    }

    static Stream<Arguments> merchantWithOrderIdArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "54907906-cb06-471f-8a10-7506803f3354"),
                Arguments.of(m, "5e587b13-ac58-4877-8ccd-31581c4a5a1f")
        ));
    }
}
