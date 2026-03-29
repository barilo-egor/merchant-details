package tgb.cryptoexchange.merchantdetails.details.yolo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequestWithMethod;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.YoloPropertiesImpl;
import tgb.cryptoexchange.merchantdetails.service.RequestService;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YoloOrderCreationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private WebClient webClient;
    @Mock
    private YoloPropertiesImpl yoloProperties;
    @Mock
    private CallbackConfig callbackConfig;

    private YoloOrderCreationServiceImpl yoloService;
    @Mock
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        yoloService = new YoloOrderCreationServiceImpl(webClient, yoloProperties, callbackConfig);
        ReflectionTestUtils.setField(yoloService, "requestService", requestService);
        ReflectionTestUtils.setField(yoloService, "objectMapper", objectMapper);
    }

    @Test
    void getMerchant_ShouldReturnYolo() {
        assertEquals(Merchant.YOLO, yoloService.getMerchant());
    }

    @Test
    void uriBuilderShouldAddPathAndQuery() {
        when(yoloProperties.accountId()).thenReturn("testId");
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI resultUri = yoloService.uriBuilder(null).apply(uriBuilder);

        assertEquals("/api/client/orders/deposit", resultUri.getPath());
        assertEquals("accountId=testId", resultUri.getQuery());
        assertEquals("/api/client/orders/deposit?accountId=testId", resultUri.toString());
    }

    @ValueSource(strings = {
            "iXyHJsfsffWW2zMDavNqGI", "nDqwHgf5OggggQX0hr5baF"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String storeKey) {
        String fakeToken = "mock_token_123";
        Object jwtDataObj = ReflectionTestUtils.getField(yoloService, "jwtData");
        ReflectionTestUtils.setField(jwtDataObj, "accessToken", fakeToken);
        ReflectionTestUtils.setField(jwtDataObj, "expiresAt", Instant.now().plus(10, ChronoUnit.MINUTES));

        when(yoloProperties.storeKey()).thenReturn(storeKey);


        HttpHeaders headers = new HttpHeaders();
        yoloService.headers(null, null).accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(storeKey, headers.getFirst("X-Store-Key")),
                () -> assertEquals("Bearer " + fakeToken, headers.getFirst("Authorization"))
        );

        verify(requestService, never()).request(any(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "iXyHJsfsffWW2zMDavNqGI", "nDqwHgf5OggggQX0hr5baF"
    })
    void headersShouldRefreshStoreTokenIfExpired(String storeKey) {
        String oldToken = "old_expired_token";
        String newToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidGMyNG1vbmV5QHNlY3JldHQubmV0IiwiaWF0IjoxNzcyNjI4ODUyLCJleHAiOjE3NzI3MTUyNTJ9.RF_EYOCCK6kL0zQnQcJQWP_cBDXx6GOxN_PPJxsp_S0";
        String formattedDate = Instant.now().plus(1, ChronoUnit.HOURS).toString();
        String jsonResponse = String.format("{\"twoFaType\":\"DISABLED\",\"permissions\":[],\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidGMyNG1vbmV5QHNlY3JldHQubmV0IiwiaWF0IjoxNzcyNjI4ODUyLCJleHAiOjE3NzI3MTUyNTJ9.RF_EYOCCK6kL0zQnQcJQWP_cBDXx6GOxN_PPJxsp_S0\",\"secretKey\":\"r2XQ8jAYVSb00VzgkgmH5wXwXRTnPoOKYK89FOCj+LbmSXR/lR1aq88aoWkb7ohvk3qDoJouyNBxIRA9hWpQ1Sc0o0xhFfwpsJ8XTRbGaShlaW+LAcxd3g==\",\"expiresAt\":\"%s\"}", formattedDate);

        Object jwtDataObj = ReflectionTestUtils.getField(yoloService, "jwtData");
        ReflectionTestUtils.setField(jwtDataObj, "accessToken", oldToken);
        ReflectionTestUtils.setField(jwtDataObj, "expiresAt", Instant.now().minus(10, ChronoUnit.MINUTES));

        when(yoloProperties.storeKey()).thenReturn(storeKey);
        when(requestService.request(any(), any(), any(), any(), any())).thenReturn(jsonResponse);
        HttpHeaders headers = new HttpHeaders();
        yoloService.headers(null, null).accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals(storeKey, headers.getFirst("X-Store-Key")),
                () -> assertEquals("Bearer " + newToken, headers.getFirst("Authorization"))
        );
        verify(requestService, times(1)).request(any(), any(), any(), any(), any());
    }


    @Test
    void headers_ShouldRefreshToken_WhenTokenIsNull() {
        String mockJwtJson = "{\"accessToken\":\"new_token\",\"expiresAt\":\"2025-12-31T23:59:59Z\"}";

        when(requestService.request(any(), any(), any(), any(), any()))
                .thenReturn(mockJwtJson);


        Consumer<HttpHeaders> headersConsumer = yoloService.headers(new DetailsRequest(), "body");
        headersConsumer.accept(new HttpHeaders());

        verify(requestService, times(1)).request(
                eq(webClient),
                any(),
                any(),
                any(),
                any()
        );

        Object jwtData = ReflectionTestUtils.getField(yoloService, "jwtData");
        String currentToken = (String) ReflectionTestUtils.getField(jwtData, "accessToken");
        assertEquals("new_token", currentToken);
    }

    @Test
    void body_ShouldMapCorrectly() {
        DetailsRequest request = new DetailsRequest();
        request.setAmount(1000);
        List<DetailsRequest.MerchantMethod> methods = new ArrayList<>();
        methods.add(DetailsRequest.MerchantMethod.builder().merchant(Merchant.YOLO).method(Collections.singletonList("SBP")).build());
        request.setMethods(methods);

        when(callbackConfig.getGatewayUrl()).thenReturn("https://test.com");
        when(callbackConfig.getCallbackSecret()).thenReturn("secret123");

        Request result = yoloService.body(new DetailsRequestWithMethod(request, "SBP"));

        assertNotNull(result.getExternalId());
        assertEquals("1000", result.getValue());
        assertTrue(result.getWebhookUrl().contains("merchant=YOLO"));
        assertTrue(result.getWebhookUrl().contains("secret=secret123"));
    }

    @Test
    void buildResponse_ShouldUseContactNumber_WhenPresent() {
        Response response = new Response();
        response.setBankName("Sber");
        response.setContactNumber("79991234567");
        response.setOrderId("order_1");
        response.setValue(Double.valueOf("500"));

        Optional<DetailsResponse> result = yoloService.buildResponse(response);

        assertTrue(result.isPresent());
        assertEquals("Sber 79991234567", result.get().getDetails());
        assertEquals("order_1", result.get().getMerchantOrderId());
    }

    @Test
    void buildResponse_ShouldUseAccountNumber_WhenContactMissing() {
        Response response = new Response();
        response.setBankName("Tinkoff");
        response.setAccountNumber("123456789");
        response.setOrderId("order_2");
        response.setValue(Double.valueOf("100"));

        Optional<DetailsResponse> result = yoloService.buildResponse(response);

        assertTrue(result.isPresent());
        assertEquals("Tinkoff 123456789", result.get().getDetails());
    }
}