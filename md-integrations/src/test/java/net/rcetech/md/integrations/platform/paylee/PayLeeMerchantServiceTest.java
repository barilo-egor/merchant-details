package net.rcetech.md.integrations.platform.paylee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.util.MultiValueMap;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PayLeePropertiesImpl;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayLeeMerchantServiceTest {

    @Mock
    private PayLeePropertiesImpl payLeeProperties;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private PayLeeMerchantServiceImpl payLeeMerchantService;

    @Test
    void getMerchantShouldReturnPayLee() {
        assertEquals(Merchant.PAY_LEE, payLeeMerchantService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/partners/purchases/", payLeeMerchantService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        when(payLeeProperties.token()).thenReturn(token);
        payLeeMerchantService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Token " + token, headers.getFirst(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE))
        );
    }

    @CsvSource(textBlock = """
            5600,CARD
            2504,SBP
            """)
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());
        detailsRequest.setUserId(String.valueOf(1231231231L));
        Request actual = payLeeMerchantService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getPrice()),
                () -> assertEquals(method, actual.getRequisitesType()),
                () -> assertNotNull(actual.getClientId()),
                () -> assertEquals(8, actual.getClientId().length()),
                () -> assertEquals("USDT", actual.getAmountCurrency())
        );
    }

    @CsvSource(textBlock = """
            Альфа банк,1234123412341234,55003,PENDING,55004
            Тинькофф,79869869898,2400,COMPLETED,2399
            """)
    @ParameterizedTest
    void buildResponseShouldBuildResponseObject(String bankName, String requisites, Integer id, Status status, Double price) {
        Response response = new Response();
        response.setBankName(bankName);
        response.setRequisites(requisites);
        response.setId(id);
        response.setStatus(status);
        response.setPrice(price);

        Optional<DetailsResponse> maybeResponse = payLeeMerchantService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bankName, actual.getBank()),
                () -> assertEquals(requisites, actual.getDetails()),
                () -> assertEquals(Merchant.PAY_LEE, actual.getMerchant()),
                () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(price.intValue(), actual.getAmount())
        );
    }

    @Test
    void buildResponseShouldSetRequisitesIfHasNotQRMethod() {
        Response response = new Response();
        response.setBankName("bankName");
        response.setRequisites("requisites");
        response.setId(1);
        response.setStatus(Status.PENDING);
        response.setPrice(1000.0);
        response.setRequisitesType(List.of(Method.CARD));
        Optional<DetailsResponse> maybeResponse = payLeeMerchantService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals("bankName", actual.getBank());
        assertEquals("requisites", actual.getDetails());
        assertNull(actual.getQr());
    }

    @Test
    void buildResponseShouldSetQRIfHasQRMethod() {
        Response response = new Response();
        response.setBankName("bankName");
        response.setRequisites("requisites");
        response.setId(1);
        response.setStatus(Status.PENDING);
        response.setPrice(1000.0);
        response.setRequisitesType(List.of(Method.ANY_QR));
        Optional<DetailsResponse> maybeResponse = payLeeMerchantService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals("requisites", actual.getQr());
        assertNull(actual.getBank());
        assertNull(actual.getDetails());
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoTraderError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
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
        assertTrue(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotNoTraderError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
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
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArrayIsEmpty() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(0);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {2, 10, 100})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArraySizeMoreThan1(int size) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(size);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNonFieldErrorsIsNotArray() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoNonFieldErrors() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(Mockito.anyString())).thenThrow(JacksonException.class);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionIsNotBadRequest() {
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(HttpClientErrorException.Conflict.class)));
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(HttpServerErrorException.InternalServerError.class)));
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(RuntimeException.class)));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
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
        assertTrue(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotAmountError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
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
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0, 2, 10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceSizeNot1(int size) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
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
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceIsNotArray() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoPrice() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("nonFieldErrors")).thenReturn(false);
        when(response.has("price")).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Captor
    private ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @ValueSource(strings = {"54907906-cb06-471f-8a10-7506803f3354", "5e587b13-ac58-4877-8ccd-31581c4a5a1f"})
    @ParameterizedTest
    void sendReceiptShouldCallRequestServiceMethod(String orderId) {
        RequestService requestService = Mockito.mock(RequestService.class);
        payLeeMerchantService.setRequestService(requestService);
        byte[] fileContent = "test content".getBytes();
        String fileName = "receipt.jpg";
        payLeeMerchantService.sendReceipt(orderId, fileContent, fileName);
        when(payLeeProperties.token()).thenReturn("token");
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
}