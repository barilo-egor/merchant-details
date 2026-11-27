package net.rcetech.md.integrations.platform.baypay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.BayPayProperties;
import net.rcetech.md.integrations.service.RequestService;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов baypay без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт baypay попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class BayPayAccountServiceTest {

    private final UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

    @Mock
    private RestClient restClient;

    @Mock
    private BayPayProperties bayPayProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private RequestService requestService;

    static final String[] ACCOUNTS = {"BAY_PAY", "BAY_PAY_HIGH_CHECK", "BAY_PAY_LOW_CHECK"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private BayPayOrderCreationService service(Merchant merchant) {
        BayPayOrderCreationService service = new BayPayOrderCreationService(merchant, restClient,
                bayPayProperties, callbackConfig);
        service.setRequestService(requestService);
        return service;
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BAY_PAY", "BAY_PAY_HIGH_CHECK", "BAY_PAY_LOW_CHECK"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BAY_PAY", "BAY_PAY_HIGH_CHECK", "BAY_PAY_LOW_CHECK"})
    void shouldBuildCorrectCreationUri(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();

        Function<UriBuilder, URI> builderFunction = service(merchant).uriBuilder(detailsRequest);
        URI uri = builderFunction.apply(uriBuilder);

        assertEquals("/s2s/invoices/create/", uri.toString());
    }

    @MethodSource("merchantWithApiKeyArgs")
    @ParameterizedTest
    void shouldPopulateRequiredHeaders(Merchant merchant, String apiKey) {
        when(bayPayProperties.apiKey()).thenReturn(apiKey);

        HttpHeaders headers = new HttpHeaders();
        Consumer<HttpHeaders> headersConsumer = service(merchant).headers(new OrderCreationRequest(), null);
        headersConsumer.accept(headers);

        assertAll(
                () -> assertEquals("application/json", headers.getFirst("Content-Type")),
                () -> assertEquals("Bearer " + apiKey, headers.getFirst("Authorization"))
        );
    }

    @MethodSource("merchantWithBodyArgs")
    @ParameterizedTest
    void shouldBuildRequestBodyCorrectly(Merchant merchant, Integer amount, String method, String gatewayUrl,
                                         String callbackSecret) {
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);
        when(callbackConfig.getCallbackSecret()).thenReturn(callbackSecret);

        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);

        Request actual = service(merchant).body(detailsRequest);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(amount, actual.getAmount()),
                () -> assertEquals(Method.valueOf(method), actual.getMethod()),
                () -> assertNotNull(actual.getOrderId()),
                () -> assertEquals(36, actual.getOrderId().length()),
                () -> assertEquals(
                        gatewayUrl + "/merchant-details/callback?merchant=" + merchant.name() + "&secret=" + callbackSecret,
                        actual.getCallbackUrl()
                )
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BAY_PAY", "BAY_PAY_HIGH_CHECK", "BAY_PAY_LOW_CHECK"})
    void shouldBuildDetailsResponseFromGatewayResponse(Merchant merchant) {
        Response gatewayResponse = new Response();
        Response.Data data = new Response.Data();
        data.setId("inv_987456");
        data.setStatus(Status.ACTIVE);
        data.setAmount("25000");

        Response.Data.PaymentDetail paymentDetail = new Response.Data.PaymentDetail();
        paymentDetail.setBank("Сбербанк");
        paymentDetail.setDetail("2202202200001111");
        data.setPaymentDetail(paymentDetail);

        gatewayResponse.setData(data);

        Optional<DetailsResponse> actualOptional = service(merchant).buildResponse(gatewayResponse);

        assertTrue(actualOptional.isPresent());
        DetailsResponse actual = actualOptional.get();

        assertAll(
                () -> assertEquals(merchant, actual.getMerchant()),
                () -> assertEquals("ACTIVE", actual.getMerchantOrderStatus()),
                () -> assertEquals("inv_987456", actual.getMerchantOrderId()),
                () -> assertEquals(25000, actual.getAmount()),
                () -> assertEquals("Сбербанк", actual.getBank()),
                () -> assertEquals("2202202200001111", actual.getDetails())
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"BAY_PAY", "BAY_PAY_HIGH_CHECK", "BAY_PAY_LOW_CHECK"})
    void shouldMakeCorrectCancelRequest(Merchant merchant) {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId("baypay_order_777");

        service(merchant).makeCancelRequest(cancelOrderRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);

        verify(requestService).request(
                eq(restClient),
                eq(HttpMethod.POST),
                uriCaptor.capture(),
                any(),
                isNull()
        );

        URI uri = uriCaptor.getValue().apply(uriBuilder);
        assertEquals("/s2s/invoice/baypay_order_777/cancel/", uri.toString());
    }

    static Stream<Arguments> merchantWithApiKeyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "test-secret-api-key"),
                Arguments.of(m, "another-api-key")
        ));
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 15000, "CARD", "https://api.exchange.com", "cb_secret_xyz"),
                Arguments.of(m, 500, "SBP", "https://gateway.example.com", "secret_42")
        ));
    }
}
