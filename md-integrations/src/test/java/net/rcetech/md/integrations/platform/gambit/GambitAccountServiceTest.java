package net.rcetech.md.integrations.platform.gambit;

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
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.integrations.platform.OrderCreationRequest;
import net.rcetech.md.meta.exception.MerchantMethodNotFoundException;
import net.rcetech.md.integrations.properties.GambitProperties;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов gambit/hesoyam без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт gambit попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class GambitAccountServiceTest {

    @Mock
    private GambitProperties gambitProperties;

    @Mock
    private RestClient restClient;

    static final String[] ACCOUNTS = {"GAMBIT", "HESOYAM"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private GambitOrderCreationService service(Merchant merchant) {
        return new GambitOrderCreationService(merchant, restClient, gambitProperties);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GAMBIT", "HESOYAM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GAMBIT", "HESOYAM"})
    void uriBuilderShouldAddPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/orders/init", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @MethodSource("merchantWithKeyArgs")
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(Merchant merchant, String key) {
        when(gambitProperties.key()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("Bearer " + key, Objects.requireNonNull(headers.get("Authorization")).getFirst()),
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst()));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GAMBIT", "HESOYAM"})
    void bodyShouldMapAmount(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(5000);
        detailsRequest.setMethod("CARD");
        Request actual = service(merchant).body(detailsRequest);
        assertEquals(5000, new BigDecimal(actual.getAmount()).intValue());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GAMBIT", "HESOYAM"})
    void bodyShouldParseMethodByMerchant(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        if (Merchant.HESOYAM == merchant) {
            detailsRequest.setMethod("QR");
            assertEquals(HesoyamMethod.QR, service(merchant).body(detailsRequest).getMethod());
        } else {
            detailsRequest.setMethod("MOBILE");
            assertEquals(GambitMethod.MOBILE, service(merchant).body(detailsRequest).getMethod());
        }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GAMBIT", "HESOYAM"})
    void bodyShouldThrowIfMethodIsOfAnotherAccount(Merchant merchant) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Merchant.HESOYAM == merchant ? "MOBILE" : "QR");
        assertThrows(MerchantMethodNotFoundException.class, () -> service(merchant).body(detailsRequest));
    }

    @MethodSource("merchantWithCardResponseArgs")
    @ParameterizedTest
    void buildResponseShouldBuildIfCardResponseObject(Merchant merchant, Double amount, String id, Status status,
                                                      String bankName, String cardValue) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBankName(bankName);
        requisite.setCardNumber(cardValue);
        response.setPaymentDetails(requisite);

        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        assertAll(
                () -> assertEquals(amount.intValue(), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bankName, actual.getBank()),
                () -> assertEquals(cardValue, actual.getDetails()),
                () -> assertEquals(merchant, actual.getMerchant())
        );
    }

    @MethodSource("merchantWithPhoneResponseArgs")
    @ParameterizedTest
    void buildResponseShouldBuildIfPhoneResponseObject(Merchant merchant, Double amount, String id, Status status,
                                                       String bankName, String phoneValue) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setAmount(amount);

        Response.Requisites requisite = new Response.Requisites();
        requisite.setBankName(bankName);
        requisite.setPhone(phoneValue);
        response.setPaymentDetails(requisite);

        Optional<DetailsResponse> maybeResponse = service(merchant).buildResponse(response);

        DetailsResponse actual = maybeResponse.get();

        assertAll(
                () -> assertEquals(amount.intValue(), actual.getAmount()),
                () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(status.name(), actual.getMerchantOrderStatus()),
                () -> assertEquals(bankName, actual.getBank()),
                () -> assertEquals(phoneValue, actual.getDetails()),
                () -> assertEquals(merchant, actual.getMerchant())
        );
    }

    static Stream<Arguments> merchantWithKeyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "JQX1BI3Vs36UnMBC"),
                Arguments.of(m, "y701U9erXYfOAdXC")
        ));
    }

    static Stream<Arguments> merchantWithCardResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 1000.0, "ID-444", Status.SUCCEEDED, "Сбербанк", "2200111122223333"),
                Arguments.of(m, 500.0, "ID-555", Status.CANCELLED, "Альфа-Банк", "4444555566667777")
        ));
    }

    static Stream<Arguments> merchantWithPhoneResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 1000.0, "ID-444", Status.SUCCEEDED, "Сбербанк", "88005553535"),
                Arguments.of(m, 500.0, "ID-555", Status.CANCELLED, "Альфа-Банк", "88005553535")
        ));
    }
}
