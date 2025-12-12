package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtasyPayOrderCreationServiceTest {

    @Mock
    private ExtasyPayProperties extasyPayProperties;

    @Mock
    private RequestService requestService;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private ExtasyPayOrderCreationService extasyPayOrderCreationService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void getMerchantShouldReturnExtasyPay() {
        assertEquals(Merchant.EXTASY_PAY, extasyPayOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldSetPathDependsOnMethod(Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EXTASY_PAY).method(method.name()).build()));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(), extasyPayOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "ixoQTh3Gf8mY3ik", "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String token) {
        when(extasyPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        extasyPayOrderCreationService.headers(null, null).accept(headers);
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
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);

        Request actual = extasyPayOrderCreationService.body(detailsRequest);

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

        Optional<DetailsResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EXTASY_PAY, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(bank + " " + requisiteString, detailsResponse.getDetails()),
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

        Optional<DetailsResponse> maybeRequisiteResponse = extasyPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals("bank" + " " + requisiteString, detailsResponse.getDetails())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasCode1AndMessageAndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asText()).thenReturn("Unable to get requisites.");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithInternalServerErrorMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        when(response.has("code")).thenReturn(false);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Internal Server Error");
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithNotInternalServerErrorMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(true);
        when(response.has("code")).thenReturn(false);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(response.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Another error");
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }
    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfInternalServerErrorWithoutMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(false);
        when(response.has("code")).thenReturn(false);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrownAndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionNotInternalServerErrorAndInternalServerError() {
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(WebClientResponseException.BadRequest.class)
        ));
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(WebClientResponseException.Conflict.class)
        ));
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(
                Mockito.mock(RuntimeException.class)
        ));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotUnableToGetRequisitesAndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        JsonNode messageNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        when(messageNode.asText()).thenReturn("AnotherError");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        when(responseNode.get("message")).thenReturn(messageNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotMessageAndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(1);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.has("message")).thenReturn(false);
        when(responseNode.get("code")).thenReturn(codeNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNot1AndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(2);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoCodeAndInternalServerError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(false);
        WebClientResponseException.InternalServerError ex = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasAmountErrorAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
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
        when(message.asText()).thenReturn("Amount should be grater than 7000.");
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertTrue(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfMessageNotAmountShouldBeAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
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
        when(message.asText()).thenReturn("Another amount error.");
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @ValueSource(ints = {0, 2, 10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountSizeNot1AndUnprocessableEntity(int size) throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(size);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountNotArrayAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(false);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfErrorsHasNoAmountAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("amount")).thenReturn(false);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoErrorsAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("422");
        when(response.get("code")).thenReturn(code);
        when(response.has("errors")).thenReturn(false);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfCodeNot422AndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asText()).thenReturn("421");
        when(response.get("code")).thenReturn(code);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoCodeAndUnprocessableEntity() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        extasyPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(false);
        WebClientResponseException.UnprocessableEntity ex = Mockito.mock(WebClientResponseException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(extasyPayOrderCreationService.isNoDetailsExceptionPredicate().test(ex));
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,SBP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        extasyPayOrderCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        extasyPayOrderCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(webClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/transactions/" + orderId + "/cancel", uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }


}