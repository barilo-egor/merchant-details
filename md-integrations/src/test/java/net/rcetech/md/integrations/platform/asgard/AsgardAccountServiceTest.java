package net.rcetech.md.integrations.platform.asgard;

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
import net.rcetech.md.integrations.properties.AsgardProperties;
import net.rcetech.md.integrations.service.SignatureService;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов asgard без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт asgard попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class AsgardAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private AsgardProperties asgardProperties;

    @Mock
    private SignatureService signatureService;

    @Mock
    private CallbackConfig callbackConfig;

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ASGARD", "ASGARD_HIGH_CHECK", "ASGARD_SIM", "ASGARD_WT"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ASGARD", "ASGARD_HIGH_CHECK", "ASGARD_SIM", "ASGARD_WT"})
    void uriBuilderShouldReturnCorrectPath(Merchant merchant) {
        OrderCreationRequest request = new OrderCreationRequest();
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();

        Function<UriBuilder, URI> resultFunc = service(merchant).uriBuilder(request);
        URI uri = resultFunc.apply(uriBuilder);

        assertEquals("/payments", uri.getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithHeaderArgs")
    void headersShouldAddRequiredHeaders(Merchant merchant, String token, String secret, String signature) {
        String body = "{\"test\":\"json\"}";

        when(asgardProperties.token()).thenReturn(token);
        when(asgardProperties.secret()).thenReturn(secret);
        when(signatureService.hmacSHA256(body, secret)).thenReturn(signature);

        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = service(merchant).headers(new OrderCreationRequest(), body);
        headersConsumer.accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals("Bearer " + token, headers.getFirst("Authorization")),
                () -> assertEquals(signature, headers.getFirst("Signature"))
        );
    }

    @ParameterizedTest
    @MethodSource("merchantWithBodyArgs")
    void bodyShouldMapRequestCorrectly(Merchant merchant, Integer amount, String method, String merchantId,
                                       String gatewayUrl, String callbackSecret) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);

        when(asgardProperties.merchantId()).thenReturn(merchantId);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(callbackSecret);

        Request result = service(merchant).body(detailsRequest);

        assertAll(
                () -> assertNotNull(result.getOrderId()),
                () -> assertEquals(merchantId, result.getMerchantId()),
                () -> assertEquals(amount, result.getAmount()),
                () -> assertTrue(result.getCallbackUri().contains("merchant=" + merchant.name())),
                () -> assertTrue(result.getCallbackUri().contains("secret=" + callbackSecret))
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"ASGARD", "ASGARD_HIGH_CHECK", "ASGARD_SIM", "ASGARD_WT"})
    void buildResponseShouldMapProperties(Merchant merchant) {
        Response response = new Response();
        Response.Requisites requisites = new Response.Requisites();
        requisites.setId("order-1");
        requisites.setState(Status.CREATED);
        requisites.setMethod(Method.CARD);
        requisites.setBankName("Sber");
        requisites.setAmount(5000.0);
        requisites.setAddress("1234 56xx");
        response.setRequisites(requisites);

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertTrue(result.isPresent());
        DetailsResponse dr = result.get();
        assertAll(
                () -> assertEquals(merchant, dr.getMerchant()),
                () -> assertEquals("order-1", dr.getMerchantOrderId()),
                () -> assertEquals("CREATED", dr.getMerchantOrderStatus()),
                () -> assertEquals("1234 56xx", dr.getDetails()),
                () -> assertEquals("Sber", dr.getBank()),
                () -> assertEquals("CARD", dr.getPaymentMethod())
        );
    }

    static final String[] ACCOUNTS = {"ASGARD", "ASGARD_HIGH_CHECK", "ASGARD_SIM", "ASGARD_WT"};

    static Stream<Merchant> accounts() {
        return Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithHeaderArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "test-token", "test-secret", "test-signature"),
                Arguments.of(m, "other-token", "other-secret", "other-signature")));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 5936, "CARD", "M-123", "https://test.com", "cb-secret"),
                Arguments.of(m, 1000, "SBP", "M-456", "https://gateway.example.com", "secret-2")));
    }

    private AsgardOrderCreationService service(Merchant merchant) {
        return new AsgardOrderCreationService(merchant, restClient, asgardProperties,
                signatureService, callbackConfig);
    }
}
