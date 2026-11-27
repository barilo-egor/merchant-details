package net.rcetech.md.integrations.platform.cube;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.CubeProperties;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов cube без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт cube попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class CubeAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private CubeProperties cubeProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"CUBE", "CUBE_HIGH_CHECK", "CUBE_LOW_CHECK", "CUBE_SIM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"CUBE", "CUBE_HIGH_CHECK", "CUBE_LOW_CHECK", "CUBE_SIM"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/transactions/payin", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithHeaderArgs")
    void headersShouldAddRequiredHeaders(Merchant merchant, String key, String privateKey) {
        HttpHeaders headers = new HttpHeaders();
        when(cubeProperties.key()).thenReturn(key);
        when(cubeProperties.privateKey()).thenReturn(privateKey);
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(privateKey, headers.getFirst("Apiprivate")),
                () -> assertEquals(key, headers.getFirst("Apipublic")),
                () -> assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldBuildRequestObject(Merchant merchant, Integer amount, Method method) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setId("123456789");
        detailsRequest.setUserId("1231231231");
        detailsRequest.setMethod(method.name());
        Request actual = service(merchant).body(detailsRequest);
        assertAll(
                () -> assertEquals(amount, Integer.valueOf(actual.getAmount())),
                () -> assertEquals(method, actual.getMethod()),
                () -> assertNotNull(actual.getExternalId()),
                () -> assertNotNull(actual.getCallbackUrl())
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithCallbackArgs")
    void bodyShouldSetCallbackUrlCorrectly(Merchant merchant, String gatewayUrl, String secret, String dealId) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod("CARD");
        detailsRequest.setId(dealId);

        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);

        Request request = service(merchant).body(detailsRequest);

        assertNotNull(request.getExternalId());
        assertEquals(dealId, request.getExternalId());

        String expectedUrl = gatewayUrl + "/merchant-details/callback/" + merchant.name()
                + "/fetch-status?transactionId=" + request.getExternalId() + "&secret=" + secret;

        assertEquals(expectedUrl, request.getCallbackUrl());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"CUBE", "CUBE_HIGH_CHECK", "CUBE_LOW_CHECK", "CUBE_SIM"})
    void buildResponseShouldHandleDoubleAmount(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setInternalId("INT-123456");
        data.setStatus(Status.APPEAL);
        data.setBankName("Test Bank");
        data.setReceiver("Test Receiver");
        data.setAmount(1999.99);
        response.setData(data);

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertTrue(result.isPresent());
        assertAll(
                () -> assertEquals(merchant, result.get().getMerchant()),
                () -> assertEquals(1999, result.get().getAmount())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"CUBE", "CUBE_HIGH_CHECK", "CUBE_LOW_CHECK", "CUBE_SIM"})
    void buildResponseShouldSetBankAndDetails(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        data.setInternalId("INT-123456");
        data.setStatus(Status.APPEAL);
        data.setBankName("Test Bank");
        data.setReceiver("Test Receiver");
        data.setAmount(1000.0);
        response.setData(data);

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertTrue(result.isPresent());
        assertAll(
                () -> assertEquals("Test Bank", result.get().getBank()),
                () -> assertEquals("Test Receiver", result.get().getDetails())
        );
    }

    static final String[] ACCOUNTS = {"CUBE", "CUBE_HIGH_CHECK", "CUBE_LOW_CHECK", "CUBE_SIM"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithHeaderArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "PzeiDYNh1RTRD5d", "L76sF2r7uL1ClNF"),
                Arguments.of(m, "PzeiDYNh1rqwfRTRD5d", "L76sF2r7ufaL1ClNF")));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 5600, Method.CARD),
                Arguments.of(m, 2504, Method.SBP),
                Arguments.of(m, 2000, Method.SIM)));
    }

    static Stream<Arguments> merchantWithCallbackArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "https://api.example.com", "secret123", "123456")));
    }

    private CubeOrderCreationService service(Merchant merchant) {
        return new CubeOrderCreationService(merchant, restClient, cubeProperties, callbackConfig);
    }
}
