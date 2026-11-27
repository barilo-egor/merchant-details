package net.rcetech.md.integrations.platform.noros;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.integrations.properties.NorosProperties;
import net.rcetech.md.integrations.service.RequestService;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * Общий тест аккаунтов noros без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт noros попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class NorosAccountServiceTest {

    @Mock
    private NorosProperties norosProperties;

    @Mock
    private RestClient restClient;

    @Mock
    private RequestService requestService;

    @Captor
    private ArgumentCaptor<Function<UriBuilder, URI>> uriBuilderCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(norosProperties.key()).thenReturn("test-api-key");
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"NOROS", "NOROS_HIGH_CHECK"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"NOROS", "NOROS_HIGH_CHECK"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/transaction",
                service(merchant).uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"NOROS", "NOROS_HIGH_CHECK"})
    void shouldAddCorrectHeaders(Merchant merchant) {
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(new OrderCreationRequest(), "").accept(headers);

        assertEquals("application/json", headers.getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("test-api-key", headers.getFirst("api_key"));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"NOROS", "NOROS_HIGH_CHECK"})
    void shouldCorrectBuildBody(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.CARD.name());
        Request resultBody = service(merchant).body(detailsRequest);

        assertNotNull(resultBody.getOrderId());
        assertEquals(1000, resultBody.getAmount());
        assertEquals(Method.CARD, resultBody.getPaymentMethod());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"NOROS", "NOROS_HIGH_CHECK"})
    void shouldBuildResponseFromMerchantResponse(Merchant merchant) {
        Response merchantResponse = new Response();
        merchantResponse.setId("merchant-123");
        merchantResponse.setStatus(Status.CREATED);
        merchantResponse.setBankReceiver("Tinkoff");
        merchantResponse.setCard("22001100");
        merchantResponse.setAmount(1000);

        Optional<DetailsResponse> responseOpt = service(merchant).buildResponse(merchantResponse);

        assertTrue(responseOpt.isPresent());
        DetailsResponse result = responseOpt.get();
        assertEquals("merchant-123", result.getMerchantOrderId());
        assertEquals("CREATED", result.getMerchantOrderStatus());
        assertEquals("22001100", result.getDetails());
        assertEquals("Tinkoff", result.getBank());
        assertEquals(1000, result.getAmount());
        assertEquals(merchant, result.getMerchant());
    }

    @ParameterizedTest
    @MethodSource("merchantWithCancelArgs")
    void makeCancelRequestShouldMakeRequest(Merchant merchant, String orderId, Method method) {
        NorosOrderCreationService service = service(merchant);
        service.setRequestService(requestService);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(orderId);
        cancelOrderRequest.setMethod(method.name());
        service.makeCancelRequest(cancelOrderRequest);
        verify(requestService).request(eq(restClient), eq(HttpMethod.DELETE), uriBuilderCaptor.capture(),
                any(), eq(null));
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/transaction/" + orderId, uriBuilderCaptor.getValue().apply(uriBuilder).getPath());
    }

    static Stream<Merchant> accounts() {
        return Arrays.stream(new String[]{"NOROS", "NOROS_HIGH_CHECK"}).map(Merchant::valueOf);
    }

    static Stream<Arguments> merchantWithCancelArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "4be41169-2786-48f03-98b9-002a23417c45", Method.CARD),
                Arguments.of(m, "578f16e0-5941-430-80c3-b2d22ef302b8", Method.SBP)
        ));
    }

    private NorosOrderCreationService service(Merchant merchant) {
        return new NorosOrderCreationService(merchant, restClient, norosProperties);
    }
}
