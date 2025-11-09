package tgb.cryptoexchange.merchantdetails.details.paylee;

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
import tgb.cryptoexchange.merchantdetails.properties.PayLeeProperties;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayLeeMerchantServiceTest {

    @Mock
    private PayLeeProperties payLeeProperties;

    @InjectMocks
    private PayLeeMerchantService payLeeMerchantService;

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
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method.name());

        Request actual = payLeeMerchantService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getPrice()),
                () -> assertEquals(method, actual.getRequisiteType())
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
                () -> assertEquals(bankName + " " + requisites, actual.getDetails()),
                () -> assertEquals(Merchant.PAY_LEE, actual.getMerchant()),
                () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(price.intValue(), actual.getAmount())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoTraderError() throws JsonProcessingException {
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
        when(errorNode.asText()).thenReturn("Нет доступного трейдера для вашего запроса. Попробуйте повторить позже.");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotNoTraderError() throws JsonProcessingException {
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
        when(errorNode.asText()).thenReturn("Неправильный запрос.");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArrayIsEmpty() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(0);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {2, 10, 100})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfArraySizeMoreThan1(int size) throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(true);
        when(arrayNode.size()).thenReturn(size);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNonFieldErrorsIsNotArray() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(true);
        JsonNode arrayNode = Mockito.mock(JsonNode.class);
        when(responseNode.get(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(arrayNode);
        when(arrayNode.isArray()).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoNonFieldErrors() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(Mockito.anyString())).thenReturn(responseNode);
        when(responseNode.has(PayLeeMerchantService.NON_FIELD_ERRORS)).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(Mockito.anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionIsNotBadRequest() {
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(WebClientResponseException.Conflict.class)));
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(WebClientResponseException.InternalServerError.class)));
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(Mockito.mock(RuntimeException.class)));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountError() throws JsonProcessingException {
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
        when(messageNode.asText()).thenReturn("Убедитесь, что это значение больше 1000");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotAmountError() throws JsonProcessingException {
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
        when(messageNode.asText()).thenReturn("Другая ошибка");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0,2,10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceSizeNot1(int size) throws JsonProcessingException {
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
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfPriceIsNotArray() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("price")).thenReturn(true);
        when(response.has("nonFieldErrors")).thenReturn(false);
        JsonNode price = Mockito.mock(JsonNode.class);
        when(response.get("price")).thenReturn(price);
        when(price.isArray()).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoPrice() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        payLeeMerchantService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("nonFieldErrors")).thenReturn(false);
        when(response.has("price")).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(payLeeMerchantService.isNoDetailsExceptionPredicate().test(badRequest));
    }
}