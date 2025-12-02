package tgb.cryptoexchange.merchantdetails.details.settlex;

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
import tgb.cryptoexchange.merchantdetails.properties.SettleXProperties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettleXOrderCreationServiceTest {

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SettleXProperties settleXProperties;

    @InjectMocks
    private SettleXOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/transactions/in", service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "JQX1BI3Vs36UnMB", "y701U9erXYfOAdX", "k531JZgj6dsh7uT"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(settleXProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);
        assertAll(
            () -> assertEquals(key, headers.getFirst("x-merchant-api-key")),
            () -> assertEquals("application/json", headers.getFirst("Content-Type"))
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            5660,SBP,https://gateway.paysendmmm.online/merchant/settleX,3UMKcCFZQeFE5uk
            12504,C2C,https://gateway.paysendmmm.online/merchant/settleX,O9GFCTfz8wf7o2Q
            """)
    void body(Integer amount, Method method, String gatewayUrl, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.SETTLE_X).method(method.name()).build()));
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        Request actual = service.body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=SETTLE_X&secret="
                        + secret, actual.getCallbackUri()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getOrderId())),
                () -> assertTrue(actual.getExpiredAt().isBefore(LocalDateTime.now().plusMinutes(20)))
        );
    }

    @ParameterizedTest
    @CsvSource("""
            ALFA_BANK,1234123412341234,0ab4b1f1-14ee-4403-963d-87f6d8fd0c9b,bd77984c-14f8-4096-9ab2-730d416d9d81,CREATED
            Сбербанк,79829828282,033c2af3-d204-4957-aed4-c3f7337b809d,fc64ac4f-bdb9-4fb6-bbe9-c99671ca3796,IN_PROGRESS
            """)
    void buildResponseShouldBuildResponseObject(String bankName, String cardName, String id, String orderId, Status status) {
        Response response = new Response();
        response.setId(id);
        response.setOrderId(orderId);
        response.setStatus(status);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setBankName(bankName);
        requisites.setCardNumber(cardName);
        response.setRequisites(requisites);
        Optional<DetailsResponse> maybeResponse = service.buildResponse(response);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertAll(
                () -> assertEquals(bankName + " " + cardName, actual.getDetails()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(orderId, actual.getMerchantCustomId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(Merchant.SETTLE_X, actual.getMerchant())
        );
    }

    @Test
    void getMerchantShouldReturnSettleX() {
        assertEquals(Merchant.SETTLE_X, service.getMerchant());
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfNoRequisiteError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asText()).thenReturn("NO_REQUISITE");
        WebClientResponseException.Conflict conflict = Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertTrue(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotNoRequisiteError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(true);
        JsonNode error = Mockito.mock(JsonNode.class);
        when(response.get("error")).thenReturn(error);
        when(error.asText()).thenReturn("ANOTHER_ERROR");
        WebClientResponseException.Conflict conflict = Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfHasNoError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.has("error")).thenReturn(false);
        WebClientResponseException.Conflict conflict = Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        service.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.Conflict conflict = Mockito.mock(WebClientResponseException.Conflict.class);
        when(conflict.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(conflict));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotConflictException() {
        assertFalse(service.isNoDetailsExceptionPredicate().test(Mockito.mock(WebClientResponseException.BadRequest.class)));
        assertFalse(service.isNoDetailsExceptionPredicate().test(Mockito.mock(WebClientResponseException.InternalServerError.class)));
        assertFalse(service.isNoDetailsExceptionPredicate().test(Mockito.mock(RuntimeException.class)));
    }
}