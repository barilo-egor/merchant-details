package net.rcetech.md.integrations.platform.yolo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.YoloProperties;
import net.rcetech.md.integrations.service.RequestService;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Общий тест аккаунтов yolo без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт yolo попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class YoloAccountServiceTest {

    private final ObjectMapper objectMapper = JsonMappers.create();
    @Mock
    private RestClient restClient;
    @Mock
    private YoloProperties yoloProperties;
    @Mock
    private CallbackConfig callbackConfig;
    @Mock
    private RequestService requestService;

    static final String[] ACCOUNTS = {"YOLO", "YOLO_SIM"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private YoloOrderCreationService service(Merchant merchant) {
        YoloOrderCreationService service = new YoloOrderCreationService(merchant, restClient, yoloProperties,
                callbackConfig);
        service.setRequestService(requestService);
        service.setObjectMapper(objectMapper);
        return service;
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void uriBuilderShouldAddPathAndQuery(Merchant merchant) {
        when(yoloProperties.accountId()).thenReturn("testId");
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI resultUri = service(merchant).uriBuilder(null).apply(uriBuilder);

        assertEquals("/api/client/orders/deposit", resultUri.getPath());
        assertEquals("accountId=testId", resultUri.getQuery());
        assertEquals("/api/client/orders/deposit?accountId=testId", resultUri.toString());
    }

    @MethodSource("merchantWithStoreKeyArgs")
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(Merchant merchant, String storeKey) {
        String fakeToken = "mock_token_123";
        YoloOrderCreationService service = service(merchant);
        Object jwtDataObj = ReflectionTestUtils.getField(service, "jwtData");
        ReflectionTestUtils.setField(jwtDataObj, "accessToken", fakeToken);
        ReflectionTestUtils.setField(jwtDataObj, "expiresAt", Instant.now().plus(10, ChronoUnit.MINUTES));

        when(yoloProperties.storeKey()).thenReturn(storeKey);

        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(storeKey, headers.getFirst("X-Store-Key")),
                () -> assertEquals("Bearer " + fakeToken, headers.getFirst("Authorization"))
        );

        verify(requestService, never()).request(any(), any(), any(), any(), any());
    }

    @MethodSource("merchantWithStoreKeyArgs")
    @ParameterizedTest
    void headersShouldRefreshStoreTokenIfExpired(Merchant merchant, String storeKey) {
        String oldToken = "old_expired_token";
        String newToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidGMyNG1vbmV5QHNlY3JldHQubmV0IiwiaWF0IjoxNzcyNjI4ODUyLCJleHAiOjE3NzI3MTUyNTJ9.RF_EYOCCK6kL0zQnQcJQWP_cBDXx6GOxN_PPJxsp_S0";
        String formattedDate = Instant.now().plus(1, ChronoUnit.HOURS).toString();
        String jsonResponse = String.format("{\"twoFaType\":\"DISABLED\",\"permissions\":[],\"accessToken\":\"%s\",\"secretKey\":\"r2XQ8jAYVSb00VzgkgmH5wXwXRTnPoOKYK89FOCj+LbmSXR/lR1aq88aoWkb7ohvk3qDoJouyNBxIRA9hWpQ1Sc0o0xhFfwpsJ8XTRbGaShlaW+LAcxd3g==\",\"expiresAt\":\"%s\"}", newToken, formattedDate);

        YoloOrderCreationService service = service(merchant);
        Object jwtDataObj = ReflectionTestUtils.getField(service, "jwtData");
        ReflectionTestUtils.setField(jwtDataObj, "accessToken", oldToken);
        ReflectionTestUtils.setField(jwtDataObj, "expiresAt", Instant.now().minus(10, ChronoUnit.MINUTES));

        when(yoloProperties.storeKey()).thenReturn(storeKey);
        when(requestService.request(any(), any(), any(), any(), any())).thenReturn(jsonResponse);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(storeKey, headers.getFirst("X-Store-Key")),
                () -> assertEquals("Bearer " + newToken, headers.getFirst("Authorization"))
        );
        verify(requestService, times(1)).request(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void headersShouldRefreshTokenWhenTokenIsNull(Merchant merchant) {
        String mockJwtJson = "{\"accessToken\":\"new_token\",\"expiresAt\":\"2025-12-31T23:59:59Z\"}";

        when(requestService.request(any(), any(), any(), any(), any()))
                .thenReturn(mockJwtJson);

        YoloOrderCreationService service = service(merchant);
        Consumer<HttpHeaders> headersConsumer = service.headers(new OrderCreationRequest(), "body");
        headersConsumer.accept(new HttpHeaders());

        verify(requestService, times(1)).request(
                eq(restClient),
                any(),
                any(),
                any(),
                any()
        );

        Object jwtData = ReflectionTestUtils.getField(service, "jwtData");
        String currentToken = (String) ReflectionTestUtils.getField(jwtData, "accessToken");
        assertEquals("new_token", currentToken);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void bodyShouldMapCorrectly(Merchant merchant) {
        OrderCreationRequest request = new OrderCreationRequest();
        request.setAmount(1000);
        request.setMethod(Method.SBP.name());
        when(callbackConfig.getGatewayUrl()).thenReturn("https://test.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret123");

        Request result = service(merchant).body(request);

        assertNotNull(result.getExternalId());
        assertEquals("1000", result.getValue());
        assertTrue(result.getWebhookUrl().contains("merchant=" + merchant.name()));
        assertTrue(result.getWebhookUrl().contains("secret=secret123"));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void buildResponseShouldUseContactNumberWhenPresent(Merchant merchant) {
        Response response = new Response();
        response.setBankName("Sber");
        response.setContactNumber("79991234567");
        response.setOrderId("order_1");
        response.setValue(Double.valueOf("500"));

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertTrue(result.isPresent());
        assertEquals("79991234567", result.get().getDetails());
        assertEquals("Sber", result.get().getBank());
        assertEquals("order_1", result.get().getMerchantOrderId());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"YOLO", "YOLO_SIM"})
    void buildResponseShouldUseAccountNumberWhenContactMissing(Merchant merchant) {
        Response response = new Response();
        response.setBankName("Tinkoff");
        response.setAccountNumber("123456789");
        response.setOrderId("order_2");
        response.setValue(Double.valueOf("100"));

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertTrue(result.isPresent());
        assertEquals("123456789", result.get().getDetails());
        assertEquals("Tinkoff", result.get().getBank());
    }

    static Stream<Arguments> merchantWithStoreKeyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "iXyHJsfsffWW2zMDavNqGI"),
                Arguments.of(m, "nDqwHgf5OggggQX0hr5baF")
        ));
    }
}
