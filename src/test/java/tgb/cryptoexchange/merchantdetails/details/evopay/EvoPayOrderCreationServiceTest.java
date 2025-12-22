package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.exception.ServiceUnavailableException;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.EvoPayProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.service.SleepingService;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvoPayOrderCreationServiceTest {

    @Mock
    private RequestService requestService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EvoPayProperties evoPayProperties;

    @Mock
    private SleepingService sleepingService;

    @InjectMocks
    private EvoPayOrderCreationService evoPayOrderCreationService;

    @BeforeEach
    void setUp() {
        evoPayOrderCreationService.setRequestService(requestService);
        evoPayOrderCreationService.setObjectMapper(objectMapper);
    }

    @Test
    void getMerchantShouldReturnEvoPayMerchant() {
        assertEquals(Merchant.EVO_PAY, evoPayOrderCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddUriPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/v1/api/order/payin", evoPayOrderCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB,555",
            "y701U9erXYfOAdX,158",
            "w1vGjx4COVk531JZgj6dsh7uT,901"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeadersWithLessThan1000Amount(String key, Integer amount) {
        when(evoPayProperties.changeKey()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        evoPayOrderCreationService.headers(detailsRequest, null).accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst()),
                () -> assertEquals("application/json", headers.getFirst("Content-Type"))
        );
    }

    @CsvSource({
            "JQX1BI3Vs36UnMB,1001",
            "y701U9erXYfOAdX,15855",
            "w1vGjx4COVk531JZgj6dsh7uT,2700"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeadersWithMoreThan1000Amount(String key, Integer amount) {
        when(evoPayProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        evoPayOrderCreationService.headers(detailsRequest, null).accept(headers);
        assertAll(
                () -> assertEquals(key, Objects.requireNonNull(headers.get("x-api-key")).getFirst())
        );
    }

    @CsvSource({
            "2100,BANK_CARD",
            "2100,SBP"
    })
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Integer amount, String method) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.EVO_PAY).method(method).build()));
        Request request = evoPayOrderCreationService.body(detailsRequest);
        assertAll(
                () -> assertDoesNotThrow(() -> UUID.fromString(request.getCustomId())),
                () -> assertEquals(Method.valueOf(method), request.getPaymentMethod()),
                () -> assertEquals(amount, request.getFiatSum())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,CREATED,1234123412341234,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,IN_PROCESS,9876987654325432,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithCard(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank(bank);
        requisites.setRecipientCardNumber(requisiteString);
        response.setRequisites(requisites);
        response.setOrderStatus(status);

        Optional<DetailsResponse> maybeRequisiteResponse = evoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EVO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @CsvSource({
            "7cc259a2-67a2-4a67-8e7e-e0342c08da81,CREATED,78984512,5001,Альфа-банк",
            "418adf2c-c382-42f4-8bbc-0d0ea008f701,IN_PROCESS,73215498,1222,Сбербанк"
    })
    @ParameterizedTest
    void buildResponseShouldBuildRequisiteResponseWithSbp(String id, Status status, String requisiteString, String bank) {
        Response response = new Response();
        response.setId(id);
        Response.Requisites requisites = new Response.Requisites();
        requisites.setRecipientBank(bank);
        requisites.setRecipientPhoneNumber(requisiteString);
        response.setRequisites(requisites);
        response.setOrderStatus(status);

        Optional<DetailsResponse> maybeRequisiteResponse = evoPayOrderCreationService.buildResponse(response);
        assertTrue(maybeRequisiteResponse.isPresent());
        DetailsResponse actual = maybeRequisiteResponse.get();
        assertAll(
                () -> assertEquals(Merchant.EVO_PAY, actual.getMerchant()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bank + " " + requisiteString, actual.getDetails())
        );
    }

    @Test
    void makeRequestShouldReturnEmptyOptionalIfNoResponse() {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn(null);
        assertTrue(evoPayOrderCreationService.makeRequest(detailsRequest, "").isEmpty());
    }

    @Test
    void makeRequestShouldThrowServiceUnavailableIfJsonProcessingExceptionWasThrownWhileMappingCreateOrderResponse() throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenThrow(JsonProcessingException.class);
        assertThrows(ServiceUnavailableException.class, () -> evoPayOrderCreationService.makeRequest(detailsRequest, ""));
    }

    @Test
    void makeRequestShouldThrowServiceUnavailableIfInterruptedExceptionWasThrown() throws JsonProcessingException, InterruptedException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        doThrow(InterruptedException.class).when(sleepingService).sleep(8);
        assertThrows(ServiceUnavailableException.class, () -> evoPayOrderCreationService.makeRequest(detailsRequest, ""));
    }

    @ValueSource(strings = {
            "cbf6c676-1adb-4f03-81b0-67c26dea863c",
            "8a7e1e35-6ced-41ec-9949-acbdd52076c7"
    })
    @ParameterizedTest
    void makeRequestShouldMakeRequestToListOrdersUrlWithOrderIdParam(String id) throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        response.setId(id);
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<UriBuilder, URI>> captor =
                (ArgumentCaptor<Function<UriBuilder, URI>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Function.class);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        assertThrows(ServiceUnavailableException.class, () -> evoPayOrderCreationService.makeRequest(detailsRequest, ""));
        verify(requestService).request(any(), eq(HttpMethod.GET), captor.capture(), any(), anyString());
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI actual = captor.getValue().apply(uriBuilder);
        assertTrue(actual.toString().endsWith("/v1/api/order/list?order_id=" + id));
    }

    @Test
    void makeRequestShouldThrowServiceUnavailableIfJsonProcessingExceptionWasThrownWhileMappingGetOrderResponse() throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        assertThrows(ServiceUnavailableException.class, () -> evoPayOrderCreationService.makeRequest(detailsRequest, ""));
    }

    @Test
    void makeRequestShouldReturnEmptyOptionalIfHasNoEntries() throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("entries")).thenReturn(false);
        assertTrue(evoPayOrderCreationService.makeRequest(detailsRequest, "").isEmpty());
    }

    @Test
    void makeRequestShouldReturnEmptyOptionalIfEntriesIsNotArray() throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("entries")).thenReturn(true);
        JsonNode entries = Mockito.mock(JsonNode.class);
        when(responseNode.get("entries")).thenReturn(entries);
        when(entries.isArray()).thenReturn(false);
        assertTrue(evoPayOrderCreationService.makeRequest(detailsRequest, "").isEmpty());
    }

    @Test
    void makeRequestShouldReturnEmptyOptionalIfEntriesIsEmpty() throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("entries")).thenReturn(true);
        JsonNode entries = Mockito.mock(JsonNode.class);
        when(responseNode.get("entries")).thenReturn(entries);
        when(entries.isArray()).thenReturn(true);
        when(entries.isEmpty()).thenReturn(true);
        assertTrue(evoPayOrderCreationService.makeRequest(detailsRequest, "").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"id\":\"550e8400-e29b-41d4-a716-446655440000\",\"orderStatus\":\"SUCCESS\",\"requisites\":" +
                    "{\"recipient_phone_number\":\"+79991234567\",\"recipient_card_number\":null,\"recipient_bank\":\"Sberbank\"}}",
            "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"orderStatus\":\"PENDING\",\"requisites\":" +
                    "{\"recipient_phone_number\":null,\"recipient_card_number\":\"4111111111111111\",\"recipient_bank\":\"Tinkoff\"}}"
    })
    void makeRequestShouldReturnEmptyOptionalIfEntriesIsEmpty(String orderBody) throws JsonProcessingException {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(2000);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        Response response = new Response();
        when(objectMapper.readValue(anyString(), eq(Response.class))).thenReturn(response);
        when(requestService.request(any(), any(), any(), any(), anyString())).thenReturn("");
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(responseNode);
        when(responseNode.has("entries")).thenReturn(true);
        JsonNode entries = Mockito.mock(JsonNode.class);
        when(responseNode.get("entries")).thenReturn(entries);
        when(entries.isArray()).thenReturn(true);
        when(entries.isEmpty()).thenReturn(false);
        JsonNode order = Mockito.mock(JsonNode.class);
        when(entries.get(0)).thenReturn(order);
        when(order.toPrettyString()).thenReturn(orderBody);
        Optional<String> maybeResponse = evoPayOrderCreationService.makeRequest(detailsRequest, "");
        assertTrue(maybeResponse.isPresent());
        assertEquals(orderBody, maybeResponse.get());
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIf500() {
        WebClientResponseException.InternalServerError internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("Internal Server Error");
        assertTrue(evoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfBodyNotInternalServerError() {
        WebClientResponseException.InternalServerError internalServerError = Mockito.mock(WebClientResponseException.InternalServerError.class);
        when(internalServerError.getResponseBodyAsString()).thenReturn("Bad amount");
        assertFalse(evoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInternalServerError() {
        assertFalse(evoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.BadRequest.class)));
        assertFalse(evoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.Conflict.class)));
        assertFalse(evoPayOrderCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.UnprocessableEntity.class)));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIf502() {
        WebClientResponseException.BadGateway internalServerError = Mockito.mock(WebClientResponseException.BadGateway.class);
        assertTrue(evoPayOrderCreationService.isNoDetailsExceptionPredicate().test(internalServerError));
    }

}