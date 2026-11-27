package net.rcetech.md.integrations.platform.manypay;

import org.junit.jupiter.api.Test;
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
import net.rcetech.md.integrations.properties.ManyPayProperties;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов manypay без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт manypay попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class ManyPayAccountServiceTest {

    @Mock
    private ManyPayProperties manyPayProperties;

    @Mock
    private RestClient restClient;

    @Mock
    private CallbackConfig callbackConfig;

    static final String[] ACCOUNTS = {"MANY_PAY", "MANY_PAY_HIGH_CHECK", "MANY_PAY_LOW_CHECK"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private ManyPayOrderCreationService service(Merchant merchant) {
        return new ManyPayOrderCreationService(merchant, restClient, manyPayProperties, callbackConfig);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"MANY_PAY", "MANY_PAY_HIGH_CHECK", "MANY_PAY_LOW_CHECK"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"MANY_PAY", "MANY_PAY_HIGH_CHECK", "MANY_PAY_LOW_CHECK"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/order", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @MethodSource("merchantWithTokenArgs")
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(Merchant merchant, String token) {
        when(manyPayProperties.token()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(token, headers.getFirst("X-API-ACCESS-TOKEN")),
                () -> assertEquals("application/json", headers.getFirst("Content-Type"))
        );
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"MANY_PAY", "MANY_PAY_HIGH_CHECK", "MANY_PAY_LOW_CHECK"})
    void shouldMapDetailsRequestToRequestBody(Merchant merchant) {
        OrderCreationRequest request = mock(OrderCreationRequest.class);
        when(request.getAmount()).thenReturn(150);
        when(request.getMethod()).thenReturn("CARD");
        Request resultBody = service(merchant).body(request);

        assertThat(resultBody).isNotNull();
        assertThat(resultBody.getAmount()).isEqualTo(15000);
        assertThat(resultBody.getPaymentMethod()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"MANY_PAY", "MANY_PAY_HIGH_CHECK", "MANY_PAY_LOW_CHECK"})
    void shouldMapResponseToDetailsResponse(Merchant merchant) {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();

        requisites.setBankName("Sberbank");
        requisites.setDetails("44445555");
        data.setOrderId("order-id-999");

        data.setStatus(Status.PENDING);
        data.setPaymentDetails(requisites);
        response.setData(data);

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();

        assertThat(detailsResponse.getBank()).isEqualTo("Sberbank");
        assertThat(detailsResponse.getDetails()).isEqualTo("44445555");
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("order-id-999");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
        assertThat(detailsResponse.getMerchant()).isEqualTo(merchant);
    }

    static Stream<Arguments> merchantWithTokenArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "qweqwfqwgqw"),
                Arguments.of(m, "qwrwqrt45qwfr"),
                Arguments.of(m, "ggggggqwtttggweg3")
        ));
    }
}
