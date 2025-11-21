package tgb.cryptoexchange.merchantdetails.details.daopayments;

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
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.DaoPaymentsProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DaoPaymentsOrderCreationServiceTest {

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private DaoPaymentsProperties daoPaymentsProperties;

    @InjectMocks
    private DaoPaymentsOrderCreationService daoPaymentsOrderCreationService;


    @Test
    void getMerchantShouldReturnCrocoPayMerchant() {
        assertEquals(Merchant.DAO_PAYMENTS, daoPaymentsOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/v1/deposit", daoPaymentsOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB",
            "y701U9erXYfOAdX",
            "w1vGjx4COVk531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(daoPaymentsProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        daoPaymentsOrderCreationService.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(key, Objects.requireNonNull(headers.get("X-API-KEY")).getFirst())
        );
    }

    @CsvSource({
            "2100,https://gateway.paysendmmm.online,CARD,xgjUpv5iEOnWKN2",
            "2100,https://someaddress.online,SBP,PfWjesdX49mQSKJ"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String gatewayUrl, String method, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        String expectedCallbackUrl = gatewayUrl + "/merchant-details/callback?merchant=DAO_PAYMENTS&secret=" + secret;
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        Request request = daoPaymentsOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getMerchantOrderId())),
                () -> assertEquals(Method.valueOf(method), request.getRequisiteType()),
                () -> assertEquals(amount.toString(), request.getAmount()),
                () -> assertEquals(expectedCallbackUrl, request.getSuccessUrl()),
                () -> assertEquals(expectedCallbackUrl, request.getFailUrl())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,PENDING,1234123412341234,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,COMPLETED,9876987654325432,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponse(String id, Status status, String requisiteString, Integer amount, String bank) {
        Response response = new Response();
        response.setTransactionId(id);
        response.setStatus(status);
        response.setAmount(amount.toString());
        Response.TransferDetails transferDetails = new Response.TransferDetails();
        transferDetails.setBankName(bank);
        transferDetails.setCardNumber(requisiteString);
        response.setTransferDetails(transferDetails);

        Optional<DetailsResponse> maybeRequisiteResponse = daoPaymentsOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.DAO_PAYMENTS, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @ValueSource(strings = {
            "deposit processing failed: all traders failed, last error from last trader: Ошибка! Не удалось обработать платеж!",
            "deposit processing failed: deposit amount 997.00 is below minimum allowed amount 1000.00"
    })
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoDetailsMessage(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        daoPaymentsOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asText()).thenReturn(message);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertTrue(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfErrorMessageNotDepositFailed() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        daoPaymentsOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asText()).thenReturn("Another error");
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        daoPaymentsOrderCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(false);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingException() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        daoPaymentsOrderCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.InternalServerError internalServerError =
                Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("");
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError() {
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.UnprocessableEntity.class)));
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.Conflict.class)));
        assertFalse(daoPaymentsOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }
}