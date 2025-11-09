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
}