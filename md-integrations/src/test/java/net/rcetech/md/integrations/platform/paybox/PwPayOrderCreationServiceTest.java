package net.rcetech.md.integrations.platform.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PwPayProperties;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PwPayOrderCreationServiceTest {

    @Mock
    private PwPayProperties pwPayProperties;

    @Mock
    private RequestService requestService;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private PwPayOrderCreationService pwPayOrderCreationService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void getMerchantShouldReturnPwPay() {
        assertEquals(Merchant.PW_PAY, pwPayOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(),
                pwPayOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(pwPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod(Method.CARD.name());
        pwPayOrderCreationService.headers(creationRequest, null).accept(headers);
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
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(Method.CARD.name());

        Request actual = pwPayOrderCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId()))
        );
    }

    @CsvSource({
            "144008,1234123412341234,Статус Банк",
            "1533,9876543212345678,ALFA"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseObjectWithCardNumber(Long id, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setCardNumber(requisiteString);
        response.setBankName(bank);

        Optional<DetailsResponse> maybeRequisiteResponse = pwPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.PW_PAY, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(requisiteString, detailsResponse.getDetails()),
                () -> assertEquals(bank, detailsResponse.getBank()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus())
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
        response.setPhoneNumber(requisiteString);
        response.setBankName("bank");

        Optional<DetailsResponse> maybeRequisiteResponse = pwPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(requisiteString, detailsResponse.getDetails()),
                () -> assertEquals("bank", detailsResponse.getBank())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasCode1AndMessageAndInternalServerError()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asString()).thenReturn("Unable to get requisites.");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithInternalServerErrorMessage()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        when(response.has("code")).thenReturn(false);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Internal Server Error");
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithNotInternalServerErrorMessage()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        when(response.has("code")).thenReturn(false);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Another error");
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfInternalServerErrorWithoutMessage()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(false);
        when(response.has("code")).thenReturn(false);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrownAndInternalServerError()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionNotInternalServerErrorAndInternalServerError() {
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(HttpClientErrorException.BadRequest.class)
        ));
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(HttpClientErrorException.Conflict.class)
        ));
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(RuntimeException.class)
        ));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotUnableToGetRequisitesAndInternalServerError()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asString()).thenReturn("AnotherError");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotMessageAndInternalServerError()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(false);
        when(responseNode.get("code")).thenReturn(codeNode);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNot1AndInternalServerError()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(2);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoCodeAndInternalServerError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(false);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasAmountErrorAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(1);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(amount.get(0)).thenReturn(message);
        when(message.asString()).thenReturn("Amount should be grater than 7000.");
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfMessageNotAmountShouldBeAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(1);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(amount.get(0)).thenReturn(message);
        when(message.asString()).thenReturn("Another amount error.");
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @ValueSource(ints = { 0, 2, 10 })
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountSizeNot1AndUnprocessableEntity(int size)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(size);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountNotArrayAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(false);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfErrorsHasNoAmountAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(false);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoErrorsAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(false);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfCodeNot422AndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("421");
        when(response.get("code")).thenReturn(code);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoCodeAndUnprocessableEntity()
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        pwPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(false);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(pwPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,SBP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        pwPayOrderCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        pwPayOrderCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(restClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/transactions/" + orderId + "/cancel",
                uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

}