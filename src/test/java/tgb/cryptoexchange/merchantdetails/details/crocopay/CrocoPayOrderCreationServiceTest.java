package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import tgb.cryptoexchange.merchantdetails.properties.CrocoPayProperties;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrocoPayOrderCreationServiceTest {

    @Mock
    private CrocoPayProperties crocoPayProperties;

    @InjectMocks
    private CrocoPayOrderCreationService crocoPayOrderCreationService;


    @Test
    void getMerchantShouldReturnCrocoPayMerchant() {
        assertEquals(Merchant.CROCO_PAY, crocoPayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v2/h2h/invoices", crocoPayOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "BTC24MONEY,JQX1BI3Vs36UnMB",
            "4aSoUSDBz2,y701U9erXYfOAdX",
            "id,w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String id, String secret) {
        when(crocoPayProperties.clientId()).thenReturn(id);
        when(crocoPayProperties.clientSecret()).thenReturn(secret);
        HttpHeaders headers = new HttpHeaders();
        crocoPayOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(id, Objects.requireNonNull(headers.get("Client-Id")).getFirst()),
                () -> assertEquals(secret, Objects.requireNonNull(headers.get("Client-Secret")).getFirst())
        );
    }

    @CsvSource({
            "2100,TO_CARD",
            "2100,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        Request request = crocoPayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, request.getAmount()),
                () -> assertEquals(Method.valueOf(method), request.getMethod())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,SUCCESS,9876987654325432"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithAnyRubBank(String id, Status status, String requisiteString) {
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

        Optional<DetailsResponse> maybeRequisiteResponse = crocoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.CROCO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(requisiteString, actual.getDetails())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,ALFA",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,SUCCESS,9876987654325432,SBER"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithNotAnyRubBank(String requisiteString, String bank) {
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

        Optional<DetailsResponse> maybeRequisiteResponse = crocoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfRequisiteNotFoundCode() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        crocoPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(response.get("code")).thenReturn(code);
        when(code.asText()).thenReturn("REQUISITE_NOT_FOUND");
        var internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertTrue(crocoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfCodeNotRequisiteNotFound() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        crocoPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(true);
        JsonNode code = Mockito.mock(JsonNode.class);
        when(response.get("code")).thenReturn(code);
        when(code.asText()).thenReturn("ANOTHER_ERROR");
        var internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoCode() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        crocoPayOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("code")).thenReturn(false);
        var internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        crocoPayOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        var internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError() {
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.BadRequest.class)));
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.Conflict.class)));
        assertFalse(crocoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }
}