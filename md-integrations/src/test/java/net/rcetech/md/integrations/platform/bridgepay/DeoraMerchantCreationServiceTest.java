package net.rcetech.md.integrations.platform.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.SignatureCreationException;
import net.rcetech.md.integrations.properties.DeoraProperties;
import net.rcetech.md.integrations.service.RequestService;
import net.rcetech.md.integrations.service.SignatureService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
class DeoraMerchantCreationServiceTest {

    @Mock
    private DeoraProperties deoraProperties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private DeoraMerchantCreationService deoraMerchantCreationService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @Test
    void getMerchantShouldReturnDeora() {
        assertEquals(Merchant.DEORA, deoraMerchantCreationService.getMerchant());
    }

    @Test
    void uriBuilderShouldSetPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices", deoraMerchantCreationService.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @CsvSource({
            "https://deora.com,HiubKWrJW8ytPGRR0E4XficeZ0ChxXHf,4u7XPRW4GzPlL187,sign1",
            "https://deora.merch.info,18td2niwHwNa2SOy3jhaQdWQrZeFgvy6,oNnM2Ud8RGA8FjCc,someSign2"
    })
    @ParameterizedTest
    void headersShouldSetRequiredHeaders(String url, String secret, String key, String sign)
            throws NoSuchAlgorithmException, InvalidKeyException {
        when(deoraProperties.url()).thenReturn(url);
        when(deoraProperties.key()).thenReturn(key);
        when(deoraProperties.secret()).thenReturn(secret);
        String expectedBody = "{\"field\": \"expectedBody\"}";
        ArgumentCaptor<String> dataArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secretArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(signatureService.hmacSHA1(dataArgumentCaptor.capture(), secretArgumentCaptor.capture())).thenReturn(sign);

        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest request = Mockito.mock(OrderCreationRequest.class);
        when(request.getMethod()).thenReturn(Method.TO_CARD.name());
        deoraMerchantCreationService.headers(request, expectedBody).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(sign, Objects.requireNonNull(headers.get("X-Signature")).getFirst()),
                () -> assertEquals("POST" + url + "/api/merchant/invoices" + expectedBody, dataArgumentCaptor.getValue()),
                () -> assertEquals(secret, secretArgumentCaptor.getValue())
        );
    }

    @Test
    void headersShouldThrowSignatureCreationException() throws NoSuchAlgorithmException, InvalidKeyException {
        OrderCreationRequest request = Mockito.mock(OrderCreationRequest.class);
        when(request.getMethod()).thenReturn(Method.TO_CARD.name());
        when(deoraProperties.url()).thenReturn("");
        when(deoraProperties.secret()).thenReturn("");
        when(signatureService.hmacSHA1(anyString(), anyString())).thenThrow(InvalidKeyException.class);
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = deoraMerchantCreationService.headers(request, "");
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @CsvSource({
            "1000,SBP,https://gateway.paysendmmm.online,13NFHS8pzxsFwZr,b2DVpRm6WXxzBvN",
            "3521,TO_CARD,https://bulba.paysendmmm.online,SP9HHlNKw0MIKas,gCQ8DmeRRWb5fVm"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(Integer amount, String method, String gatewayUrl, String token, String secret) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(deoraProperties.token()).thenReturn(token);
        detailsRequest.setMethod(method);
        Request actual = deoraMerchantCreationService.body(detailsRequest);

        assertAll(
                () -> assertEquals(amount.toString(), actual.getAmount()),
                () -> assertEquals("RUB", actual.getCurrency()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=DEORA&secret=" + secret,
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
        dealDTO.setPaymentMethod(bank.getDisplayName());
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites(requisite);
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId(id);
        Optional<DetailsResponse> actual = deoraMerchantCreationService.buildResponse(response);
        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(Merchant.DEORA, actualResponse.getMerchant()),
                () -> assertEquals(id, actualResponse.getMerchantOrderId()),
                () -> assertEquals(Status.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals(requisite, actualResponse.getDetails()),
                () -> assertEquals(bank.getDisplayName(), actualResponse.getBank())
        );
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInvoiceAmountMessage() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(node.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Invoice amount should be");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertTrue(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInvoiceAmountError() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(true);
        JsonNode message = Mockito.mock(JsonNode.class);
        when(node.get("message")).thenReturn(message);
        when(message.asString()).thenReturn("Another error");
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0, 2, 10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfResponseSizeNot1(int size) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(size);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfResponseIsNotArray() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfJsonProcessingExceptionWasThrown() throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        deoraMerchantCreationService.setObjectMapper(objectMapper);
        when(objectMapper.readTree(anyString())).thenThrow(JacksonException.class);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @Test
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotBadRequestException() {
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpServerErrorException.InternalServerError.class)));
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(HttpClientErrorException.Conflict.class)));
        assertFalse(deoraMerchantCreationService.isNoDetailsExceptionPredicate()
                .test(Mockito.mock(RuntimeException.class)));
    }

    @CsvSource("""
            4be41169-2786-48f3-98b9-002a23417c45,TO_CARD
            578f16e0-5941-4330-80c3-b2d22ef302b8,MOBILE_TOP_UP
            """)
    @ParameterizedTest
    void makeCancelRequestShouldMakeRequest(String orderId, Method method) {
        deoraMerchantCreationService.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        deoraMerchantCreationService.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(restClient), eq(HttpMethod.POST), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices/" + orderId + "/cancel", uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }
}