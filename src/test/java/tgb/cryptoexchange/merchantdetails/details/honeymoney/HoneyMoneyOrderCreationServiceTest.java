package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HoneyMoneyProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoneyMoneyOrderCreationServiceTest {

    @Mock
    private HoneyMoneyProperties honeyMoneyProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private HoneyMoneyOrderCreationService honeyMoneyOrderCreationService;

    @Test
    void getMerchantShouldReturnHoneyMoney() {
        assertEquals(Merchant.HONEY_MONEY, honeyMoneyOrderCreationService.getMerchant());
    }

    @EnumSource(Method.class)
    @ParameterizedTest
    void uriBuilderShouldAddPathDependsOnMethod(Method method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(method.name()).build()));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(method.getUri(), honeyMoneyOrderCreationService.uriBuilder(detailsRequest).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "iXyHJ2zMDavNqGI,873adbf2fe21f57428aed05465a5f09644055d240099397418f41bc59652a13e",
            "nDHgf5OQX0h5baF,7ddc9f1ebaace1bb7972a0e2ae79263108664f23a11da53ab585ed26dfeb9d4a"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String authToken, String signature) {
        when(honeyMoneyProperties.authToken()).thenReturn(authToken);
        when(signatureService.hmacSHA256(any(), any(), any())).thenReturn(signature);

        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(Method.CARD.name()).build()));
        HttpHeaders headers = new HttpHeaders();
        honeyMoneyOrderCreationService.headers(detailsRequest, "body").accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + authToken, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(signature, Objects.requireNonNull(headers.get("X-Signature")).getFirst())
        );
    }

    @CsvSource({
            "12500,CARD,https://gateway.paysendmmm.online/merchant/honeymoney,MV1xwso7dS35GCf",
            "2566,SBP,https://cryptoexchange.com/honeymoney/callback,BfFpfLSGX8lqydL"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, Method method, String gatewayUrl, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.HONEY_MONEY).method(method.name()).build()));
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        Request request = honeyMoneyOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(method.getBank(), request.getBank()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=HONEY_MONEY&secret="
                        + secret, request.getCallbackUrl()),
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getExtId())),
                () -> assertEquals("RUB", request.getCurrency())
        );
    }

    @CsvSource({
            "79876543223,ALFA,24444",
            "79876543223,T-bank,124"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithPhoneNumber(String phoneNumber, String bankName, Integer id) {
        Response response = new Response();
        response.setId(id);
        response.setPhoneNumber(phoneNumber);
        response.setBankName(bankName);
        Optional<DetailsResponse> maybeResponse = honeyMoneyOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(Merchant.HONEY_MONEY, actual.getMerchant()),
                () -> assertEquals(bankName + " " + phoneNumber, actual.getDetails()),
                () -> assertEquals(id.toString(), actual.getMerchantOrderId()),
                () -> assertEquals(Status.PENDING.name(), actual.getMerchantOrderStatus())
        );
    }

    @ValueSource(strings = {
            "1111222233334444",
            "1234123412341234"
    })
    @ParameterizedTest
    void buildResponseShouldBuildResponseObjectWithPhoneNumber(String cardNumber) {
        Response response = new Response();
        response.setId(1);
        response.setCardNumber(cardNumber);
        response.setBankName("bankName");
        Optional<DetailsResponse> maybeResponse = honeyMoneyOrderCreationService.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals("bankName " + cardNumber, actual.getDetails())
        );
    }

    @Test
    void isNoDetailsPredicateShouldReturnTrueIfNoRequisitesResponseException() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        Predicate<Exception> predicate = honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate();
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        when(jsonNode.has("detail")).thenReturn(true);
        JsonNode detailsNode = Mockito.mock(JsonNode.class);
        when(detailsNode.asText()).thenReturn("No requisites available for the moment. Please try again later.");
        when(jsonNode.get("detail")).thenReturn(detailsNode);
        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);
        assertTrue(predicate.test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingWasThrown() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        Predicate<Exception> predicate = honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate();
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        assertFalse(predicate.test(badRequest));
    }

    @Test
    void isNotDetailsPredicateShouldReturnFalseIfExceptionNotBadRequest() {
        Predicate<Exception> predicate = honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate();
        assertFalse(predicate.test(Mockito.mock(WebClientResponseException.BadGateway.class)));
        assertFalse(predicate.test(Mockito.mock(WebClientResponseException.InternalServerError.class)));
        assertFalse(predicate.test(Mockito.mock(RuntimeException.class)));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfAmountError() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("Amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("Amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(1);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(amount.get(0)).thenReturn(message);
        when(message.asText()).thenReturn("Amount must be between 5000 and 1000000");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotAmountError() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("Amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("Amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(1);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(amount.get(0)).thenReturn(message);
        when(message.asText()).thenReturn("Another error.");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0, 2, 10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfAmountSizeNot1(int size) throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("Amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("Amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(true);
        when(amount.size()).thenReturn(size);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfAmountNotArray() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("Amount")).thenReturn(true);
        JsonNode amount = Mockito.mock(JsonNode.class);
        when(errors.get("Amount")).thenReturn(amount);
        when(amount.isArray()).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfErrorsHasNoAmount() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(true);
        JsonNode errors = Mockito.mock(JsonNode.class);
        when(response.get("errors")).thenReturn(errors);
        when(errors.has("Amount")).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoErrors() throws JsonProcessingException {
        honeyMoneyOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("detail")).thenReturn(false);
        when(response.has("errors")).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(honeyMoneyOrderCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }
}