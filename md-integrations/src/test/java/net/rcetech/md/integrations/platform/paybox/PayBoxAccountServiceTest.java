package net.rcetech.md.integrations.platform.paybox;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.PayBoxProperties;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов paybox без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт paybox попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class PayBoxAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private PayBoxProperties payBoxProperties;

    @Mock
    private RequestService requestService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @MethodSource("merchantWithMethods")
    void uriBuilderShouldSetPathDependsOnMethod(Merchant merchant, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setMethod(method.name());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        assertEquals("/api/v1/transactions" + method.getUri(),
                service(merchant).uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithTokens")
    void headersShouldSetRequiredHeaders(Merchant merchant, String token) {
        when(payBoxProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest creationRequest = new OrderCreationRequest();
        creationRequest.setMethod(Method.CARD.name());
        service(merchant).headers(creationRequest, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + token, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithAmounts")
    void bodyShouldBuildRequestObject(Merchant merchant, int amount) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(Method.CARD.name());

        Request actual = service(merchant).body(detailsRequest);

        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getMerchantTransactionId()))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithCardResponseArgs")
    void buildResponseShouldBuildRequisiteResponseObjectWithCardNumber(Merchant merchant, Long id,
                                                                      String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        response.setCardNumber(requisiteString);
        response.setBankName(bank);

        Optional<DetailsResponse> maybeRequisiteResponse = service(merchant).buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(merchant, detailsResponse.getMerchant()),
                () -> assertEquals(id.toString(), detailsResponse.getMerchantOrderId()),
                () -> assertEquals(requisiteString, detailsResponse.getDetails()),
                () -> assertEquals(bank, detailsResponse.getBank()),
                () -> assertEquals(Status.PROCESS.name(), detailsResponse.getMerchantOrderStatus())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithPhoneResponseArgs")
    void buildResponseShouldBuildRequisiteResponseObjectWithPhoneNumber(Merchant merchant, String requisiteString) {
        Response response = new Response();
        response.setId(1L);
        response.setPhoneNumber(requisiteString);
        response.setBankName("bank");

        Optional<DetailsResponse> maybeRequisiteResponse = service(merchant).buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse detailsResponse = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(requisiteString, detailsResponse.getDetails()),
                () -> assertEquals("bank", detailsResponse.getBank())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasCode1AndMessageAndInternalServerError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertTrue(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithInternalServerErrorMessage(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertTrue(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInternalServerErrorWithNotInternalServerErrorMessage(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfInternalServerErrorWithoutMessage(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("message")).thenReturn(false);
        when(response.has("code")).thenReturn(false);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrownAndInternalServerError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfExceptionNotInternalServerError(Merchant merchant) {
        PayBoxOrderCreationService service = service(merchant);
        assertFalse(service.isNoDetailsExceptionPredicate().test(
                Mockito.mock(HttpClientErrorException.BadRequest.class)
        ));
        assertFalse(service.isNoDetailsExceptionPredicate().test(
                Mockito.mock(HttpClientErrorException.Conflict.class)
        ));
        assertFalse(service.isNoDetailsExceptionPredicate().test(
                Mockito.mock(RuntimeException.class)
        ));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfMessageNotUnableToGetRequisites(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotMessageAndInternalServerError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNot1AndInternalServerError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(codeNode.asInt()).thenReturn(2);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(true);
        when(responseNode.get("code")).thenReturn(codeNode);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoCodeAndInternalServerError(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("code")).thenReturn(false);
        HttpServerErrorException.InternalServerError ex = Mockito.mock(
                HttpServerErrorException.InternalServerError.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasAmountErrorAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertTrue(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfMessageNotAmountShouldBeAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @MethodSource("merchantWithSizes")
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountSizeNot1AndUnprocessableEntity(Merchant merchant, int size)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountNotArrayAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfErrorsHasNoAmountAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoErrorsAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfCodeNot422AndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(code.asString()).thenReturn("421");
        when(response.get("code")).thenReturn(code);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfHasNoCodeAndUnprocessableEntity(Merchant merchant)
            throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PayBoxOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(false);
        HttpClientErrorException.UnprocessableEntity ex = Mockito.mock(
                HttpClientErrorException.UnprocessableEntity.class);
        when(ex.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(ex));
    }

    @ParameterizedTest
    @MethodSource("merchantWithCancelArgs")
    void makeCancelRequestShouldMakeRequest(Merchant merchant, String orderId, Method method) {
        PayBoxOrderCreationService service = service(merchant);
        service.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        service.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(restClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/transactions/" + orderId + "/cancel",
                uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

    static final String[] ACCOUNTS = {"BUCKS_PAY", "BUCKS_PAY_SIM", "PW_PAY"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithMethods() {
        return accounts().flatMap(m -> Arrays.stream(Method.values()).map(method -> Arguments.of(m, method)));
    }

    static Stream<Arguments> merchantWithTokens() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "ixoQTh3Gf8mY3ik"),
                Arguments.of(m, "6pm5Px76Dyt5VXW65DjnlFgFnwO0f5II")));
    }

    static Stream<Arguments> merchantWithAmounts() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 1211),
                Arguments.of(m, 5004),
                Arguments.of(m, 522)));
    }

    static Stream<Arguments> merchantWithCardResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 144008L, "1234123412341234", "Статус Банк"),
                Arguments.of(m, 1533L, "9876543212345678", "ALFA")));
    }

    static Stream<Arguments> merchantWithPhoneResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "1234123412341234"),
                Arguments.of(m, "9876543212345678")));
    }

    static Stream<Arguments> merchantWithSizes() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 0),
                Arguments.of(m, 2),
                Arguments.of(m, 10)));
    }

    static Stream<Arguments> merchantWithCancelArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "4be41169-2786-48f3-98b9-002a23417c45", Method.CARD),
                Arguments.of(m, "578f16e0-5941-4330-80c3-b2d22ef302b8", Method.SBP)));
    }

    private PayBoxOrderCreationService service(Merchant merchant) {
        return new PayBoxOrderCreationService(merchant, restClient, payBoxProperties);
    }
}
