package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.enums.FiatCurrency;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.exception.SignatureCreationException;
import tgb.cryptoexchange.merchantdetails.properties.AlfaTeamProperties;
import tgb.cryptoexchange.merchantdetails.service.RequestService;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlfaTeamMerchantCreationServiceTest {

    @Mock
    private AlfaTeamProperties alfaTeamProperties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private AlfaTeamMerchantCreationService alfaTeamMerchantCreationService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ALFA_TEAM, alfaTeamMerchantCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldSetPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices", alfaTeamMerchantCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "https://alfa.com,HiubKWrJW8ytPGRR0E4XficeZ0ChxXHf,4u7XPRW4GzPlL187,sign1",
            "https://alfa.merch.info,18td2niwHwNa2SOy3jhaQdWQrZeFgvy6,oNnM2Ud8RGA8FjCc,someSign2"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String url, String secret, String key, String sign)
            throws NoSuchAlgorithmException, InvalidKeyException {
        when(alfaTeamProperties.url()).thenReturn(url);
        when(alfaTeamProperties.key()).thenReturn(key);
        when(alfaTeamProperties.secret()).thenReturn(secret);
        String expectedBody = "{\"field\": \"expectedBody\"}";
        ArgumentCaptor<String> dataArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secretArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(signatureService.hmacSHA1(dataArgumentCaptor.capture(), secretArgumentCaptor.capture())).thenReturn(sign);

        HttpHeaders headers = new HttpHeaders();
        DetailsRequest request = Mockito.mock(DetailsRequest.class);
        when(request.getMerchantMethod(Merchant.ALFA_TEAM)).thenReturn(Optional.of(Method.TO_CARD.name()));
        alfaTeamMerchantCreationService.headers(request, expectedBody).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(sign, Objects.requireNonNull(headers.get("X-Signature")).getFirst()),
                () -> assertEquals("POST" + url + "/api/merchant/invoices" + expectedBody, dataArgumentCaptor.getValue()),
                () -> assertEquals(secret, secretArgumentCaptor.getValue())
        );
    }

    @Test
    void headersShouldThrowSignatureCreationException() throws NoSuchAlgorithmException, InvalidKeyException {
        DetailsRequest request = Mockito.mock(DetailsRequest.class);
        when(request.getMerchantMethod(Merchant.ALFA_TEAM)).thenReturn(Optional.of(Method.TO_CARD.name()));
        when(alfaTeamProperties.url()).thenReturn("");
        when(alfaTeamProperties.secret()).thenReturn("");
        when(signatureService.hmacSHA1(anyString(), anyString())).thenThrow(InvalidKeyException.class);
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = alfaTeamMerchantCreationService.headers(request, "");
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @CsvSource({
            "1000,SBP,https://gateway.paysendmmm.online,13NFHS8pzxsFwZr,b2DVpRm6WXxzBvN",
            "3521,TO_CARD,https://bulba.paysendmmm.online,SP9HHlNKw0MIKas,gCQ8DmeRRWb5fVm"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, String method, String gatewayUrl, String token, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ALFA_TEAM).method(method).build()));
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(alfaTeamProperties.token()).thenReturn(token);

        Request actual = alfaTeamMerchantCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount.toString(), actual.getAmount()),
                () -> assertEquals(FiatCurrency.RUB.name(), actual.getCurrency()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=ALFA_TEAM&secret=" + secret,
                        actual.getNotificationUrl()),
                () -> assertEquals(token, actual.getNotificationToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getInternalId())),
                () -> assertEquals(Method.valueOf(method), actual.getPaymentOption()),
                () -> assertTrue(actual.getStartDeal())
        );
    }

    @CsvSource({
            "2ac093d0-49aa-4bc7-a7dc-61fa62820544,SBERBANK,1234123412341234",
            "9e6eee50-dcbb-47be-85a5-494db6d49157,UNISTREAM,79865431232",
            "070329a9-4f1b-4c83-a8f7-68a385bc23f3,RNCB,8888666644442222"
    })
    @ParameterizedTest
    void buildResponseShouldReturnMappedRequisiteDTO(String id, Bank bank, String requisite) {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentMethod(bank);
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites(requisite);
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId(id);
        Optional<DetailsResponse> actual = alfaTeamMerchantCreationService.buildResponse(response);
        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, actualResponse.getMerchant()),
                () -> assertEquals(id, actualResponse.getMerchantOrderId()),
                () -> assertEquals(Status.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals(bank.getDisplayName() + " " + requisite, actualResponse.getDetails())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInvoiceAmountMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(node.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Invoice amount should be");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInvoiceAmountError() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(node.get("message")).thenReturn(message);
        when(message.asText()).thenReturn("Another error");
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0,2,10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfResponseSizeNot1(int size) throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(size);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfResponseIsNotArray() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(false);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JsonProcessingException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        alfaTeamMerchantCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JsonProcessingException.class);
        WebClientResponseException.BadRequest badRequest = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotBadRequestException() {
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.InternalServerError.class)));
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(WebClientResponseException.Conflict.class)));
        assertFalse(alfaTeamMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,TO_CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,MOBILE_TOP_UP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        alfaTeamMerchantCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        alfaTeamMerchantCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(webClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices/" + orderId + "/cancel", uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }
}