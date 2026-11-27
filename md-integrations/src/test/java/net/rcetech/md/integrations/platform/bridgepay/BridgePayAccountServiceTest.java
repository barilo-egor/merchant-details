package net.rcetech.md.integrations.platform.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.SignatureCreationException;
import net.rcetech.md.integrations.properties.BridgePayProperties;
import net.rcetech.md.integrations.service.SignatureService;
import net.rcetech.md.integrations.service.SleepingService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов bridgepay без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт bridgepay попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class BridgePayAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private BridgePayProperties properties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SleepingService sleepingService;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void uriBuilderShouldSetPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/invoices", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithHeaderArgs")
    void headersShouldSetRequiredHeaders(Merchant merchant, String url, String secret, String key, String sign)
            throws NoSuchAlgorithmException, InvalidKeyException {
        when(properties.url()).thenReturn(url);
        when(properties.key()).thenReturn(key);
        when(properties.secret()).thenReturn(secret);
        String expectedBody = "{\"field\": \"expectedBody\"}";
        ArgumentCaptor<String> dataArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secretArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(signatureService.hmacSHA1(dataArgumentCaptor.capture(), secretArgumentCaptor.capture())).thenReturn(sign);

        HttpHeaders headers = new HttpHeaders();
        OrderCreationRequest request = Mockito.mock(OrderCreationRequest.class);
        when(request.getMethod()).thenReturn(Method.TO_CARD.name());
        service(merchant).headers(request, expectedBody).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()),
                () -> assertEquals(sign, Objects.requireNonNull(headers.get("X-Signature")).getFirst()),
                () -> assertEquals("POST" + url + "/api/merchant/invoices" + expectedBody, dataArgumentCaptor.getValue()),
                () -> assertEquals(secret, secretArgumentCaptor.getValue())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void headersShouldThrowSignatureCreationException(Merchant merchant) throws NoSuchAlgorithmException, InvalidKeyException {
        OrderCreationRequest request = Mockito.mock(OrderCreationRequest.class);
        when(request.getMethod()).thenReturn(Method.TO_CARD.name());
        when(properties.url()).thenReturn("");
        when(properties.secret()).thenReturn("");
        when(signatureService.hmacSHA1(anyString(), anyString())).thenThrow(InvalidKeyException.class);
        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = service(merchant).headers(request, "");
        assertThrows(SignatureCreationException.class, () -> headersConsumer.accept(headers));
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldBuildRequestObject(Merchant merchant, Integer amount, String method, String gatewayUrl,
                                      String token, String secret) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(properties.token()).thenReturn(token);
        detailsRequest.setMethod(method);
        Request actual = service(merchant).body(detailsRequest);

        assertAll(
                () -> assertEquals(amount.toString(), actual.getAmount()),
                () -> assertEquals("RUB", actual.getCurrency()),
                () -> assertEquals(gatewayUrl + "/merchant-details/callback?merchant=" + merchant.name()
                        + "&secret=" + secret, actual.getNotificationUrl()),
                () -> assertEquals(token, actual.getNotificationToken()),
                () -> assertDoesNotThrow(() -> UUID.fromString(actual.getInternalId())),
                () -> assertEquals(Method.valueOf(method), actual.getPaymentOption()),
                () -> assertTrue(actual.getStartDeal())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithResponseArgs")
    void buildResponseShouldReturnMappedRequisiteDTO(Merchant merchant, String id, Bank bank, String requisite) {
        Response response = new Response();
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentMethod(bank.getDisplayName());
        RequisitesDTO requisitesDTO = new RequisitesDTO();
        requisitesDTO.setRequisites(requisite);
        dealDTO.setRequisites(requisitesDTO);
        response.setDeals(List.of(dealDTO));
        response.setId(id);
        Optional<DetailsResponse> actual = service(merchant).buildResponse(response);
        assertTrue(actual.isPresent());
        DetailsResponse actualResponse = actual.get();
        assertAll(
                () -> assertEquals(merchant, actualResponse.getMerchant()),
                () -> assertEquals(id, actualResponse.getMerchantOrderId()),
                () -> assertEquals(Status.NEW.name(), actualResponse.getMerchantOrderStatus()),
                () -> assertEquals(requisite, actualResponse.getDetails()),
                () -> assertEquals(bank.getDisplayName(), actualResponse.getBank())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void isNoDetailsExceptionPredicateShouldReturnTrueIfInvoiceAmountMessage(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        BridgePayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertTrue(service.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNotInvoiceAmountError(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        BridgePayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
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
        assertFalse(service.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"})
    void isNoDetailsExceptionPredicateShouldReturnFalseIfNoMessage(Merchant merchant) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        BridgePayOrderCreationService service = service(merchant);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(1);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(response.get(0)).thenReturn(node);
        when(node.has("message")).thenReturn(false);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(badRequest));
    }

    @ValueSource(ints = {0, 2, 10})
    @ParameterizedTest
    void isNoDetailsExceptionPredicateShouldReturnFalseIfResponseSizeNot1(int size) throws JacksonException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        BridgePayOrderCreationService service = service(Merchant.DEORA);
        service.setObjectMapper(objectMapper);
        JsonNode response = Mockito.mock(JsonNode.class);
        when(objectMapper.readTree(anyString())).thenReturn(response);
        when(response.isArray()).thenReturn(true);
        when(response.size()).thenReturn(size);
        HttpClientErrorException.BadRequest badRequest = Mockito.mock(HttpClientErrorException.BadRequest.class);
        when(badRequest.getResponseBodyAsString()).thenReturn("");
        assertFalse(service.isNoDetailsExceptionPredicate().test(badRequest));
    }


    static final String[] ACCOUNTS = {"ALFA_TEAM", "ALFA_TEAM_QR", "ALFA_TEAM_WT", "DEORA", "DEORA_LOW_CHECK", "DEORA_PDF", "DEORA_SIM", "EXTASY_PAY", "EXTASY_PAY_PDF", "EXTASY_PAY_QR_NSPK", "EXTASY_PAY_QR_VIET", "ROSTRAST", "SOUZ", "SOUZ_PDF", "SOUZ_SBP_QR", "SOUZ_SIM", "STORM_TRADE", "STORM_TRADE_13"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithHeaderArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "https://deora.com", "HiubKWrJW8ytPGRR0E4XficeZ0ChxXHf", "4u7XPRW4GzPlL187", "sign1"),
                Arguments.of(m, "https://deora.merch.info", "18td2niwHwNa2SOy3jhaQdWQrZeFgvy6", "oNnM2Ud8RGA8FjCc", "someSign2")));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 1000, "SBP", "https://gateway.paysendmmm.online", "13NFHS8pzxsFwZr", "b2DVpRm6WXxzBvN"),
                Arguments.of(m, 3521, "TO_CARD", "https://bulba.paysendmmm.online", "SP9HHlNKw0MIKas", "gCQ8DmeRRWb5fVm")));
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "2ac093d0-49aa-4bc7-a7dc-61fa62820544", Bank.SBERBANK, "1234123412341234"),
                Arguments.of(m, "9e6eee50-dcbb-47be-85a5-494db6d49157", Bank.UNISTREAM, "79865431232"),
                Arguments.of(m, "070329a9-4f1b-4c83-a8f7-68a385bc23f3", Bank.RNCB, "8888666644442222")));
    }

    private BridgePayOrderCreationService service(Merchant merchant) {
        return new BridgePayOrderCreationService(merchant, restClient, properties,
                signatureService, callbackConfig, sleepingService);
    }
}
