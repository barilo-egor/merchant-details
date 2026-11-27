package net.rcetech.md.integrations.platform.goatx;

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
import net.rcetech.md.integrations.properties.GoatxProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Общий тест аккаунтов goatx без собственной логики. Сервис создаётся так же, как это
 * делает MerchantServiceFactory: мерчант в конструкторе, свойства — по префиксу справочника.
 * Новый аккаунт goatx попадает сюда автоматически через {@code Merchant}.
 */
@ExtendWith(MockitoExtension.class)
class GoatxAccountServiceTest {

    @Mock
    private GoatxProperties goatxProperties;

    @Mock
    private RestClient restClient;

    static final String[] ACCOUNTS = {"GOAT_X", "GOAT_X_SIM"};

    static Stream<Merchant> accounts() {
        return Stream.of(ACCOUNTS).map(Merchant::valueOf);
    }

    private GoatxMerchantOrderCreationService service(Merchant merchant) {
        return new GoatxMerchantOrderCreationService(merchant, restClient, goatxProperties);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GOAT_X", "GOAT_X_SIM"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GOAT_X", "GOAT_X_SIM"})
    void uriBuilderShouldAddUriPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/order/", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {"GOAT_X", "GOAT_X_SIM"})
    void headersShouldAddRequiredHeaders(Merchant merchant) {
        HttpHeaders headers = new HttpHeaders();
        service(merchant).headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals("application/json", Objects.requireNonNull(headers.get("Content-Type")).getFirst())
        );
    }

    @MethodSource("merchantWithBodyArgs")
    @ParameterizedTest
    void bodyShouldReturnMappedBody(Merchant merchant, Integer amount, String contractId, String method) {
        OrderCreationRequest detailsRequest = spy(new OrderCreationRequest());
        detailsRequest.setAmount(amount);
        detailsRequest.setMethod(method);
        when(goatxProperties.merchantContractId()).thenReturn(contractId);

        Request result = service(merchant).body(detailsRequest);

        assertAll(
                () -> assertEquals(amount.toString(), result.getSum()),
                () -> assertEquals(contractId, result.getContract()),
                () -> assertNotNull(result.getInvid()),
                () -> assertDoesNotThrow(() -> UUID.fromString(result.getInvid())),
                () -> assertNotNull(result.getSignature()),
                () -> assertEquals(64, result.getSignature().length()),
                () -> assertEquals(Method.valueOf(method), result.getWay())
        );
    }

    @MethodSource("merchantWithPhoneResponseArgs")
    @ParameterizedTest
    void buildResponseShouldBuildPhoneRequisiteResponse(Merchant merchant, String id, Status status,
                                                        String requisiteString, String bankName) {
        Response response = new Response();
        response.setId(id);
        response.setStatus(status);
        response.setWay(Method.SBP);
        Response.Requisite requisite = new Response.Requisite();
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName(bankName);
        requisite.setBank(bank);
        requisite.setPhoneNumber(requisiteString);
        response.setRequisite(requisite);
        Optional<DetailsResponse> maybeActual = service(merchant).buildResponse(response);
        assertTrue(maybeActual.isPresent());
        DetailsResponse actual = maybeActual.get();
        assertAll(
            () -> assertEquals(merchant, actual.getMerchant()),
            () -> assertEquals(id, actual.getMerchantOrderId()),
                () -> assertEquals(requisiteString, actual.getDetails()),
                () -> assertEquals(bankName, actual.getBank()),
            () -> assertEquals(status.name(), actual.getMerchantOrderStatus())
        );
    }

    @MethodSource("merchantWithCardResponseArgs")
    @ParameterizedTest
    void buildResponseShouldBuildCardRequisiteResponse(Merchant merchant, String requisiteString, String bankName) {
        Response response = new Response();
        response.setId("id");
        response.setStatus(Status.PENDING);
        response.setWay(Method.CARD);
        Response.Requisite requisite = new Response.Requisite();
        Response.Requisite.Bank bank = new Response.Requisite.Bank();
        bank.setName(bankName);
        requisite.setBank(bank);
        requisite.setCardNumber(requisiteString);
        response.setRequisite(requisite);
        Optional<DetailsResponse> maybeActual = service(merchant).buildResponse(response);
        assertTrue(maybeActual.isPresent());
        DetailsResponse actual = maybeActual.get();
        assertEquals(requisiteString, actual.getDetails());
        assertEquals(bankName, actual.getBank());
    }

    static Stream<Arguments> merchantWithBodyArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, 1000, "7fd54bb4-f20e-489d-a84a-4ff3490ea19e", "SBP"),
                Arguments.of(m, 5054, "33d29f27-195a-4870-94a4-1ca7af340cb9", "CARD")
        ));
    }

    static Stream<Arguments> merchantWithPhoneResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "7cc259a2-67a2-4a67-8e7e-e0342c08da81", Status.PENDING, "+7814587946", "Альфа-банк"),
                Arguments.of(m, "418adf2c-c382-42f4-8bbc-0d0ea008f701", Status.CONFIRMED, "+78944561234", "Сбербанк")
        ));
    }

    static Stream<Arguments> merchantWithCardResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "1234 1234 1234 1234", "Альфа-банк"),
                Arguments.of(m, "9999 9999 9999 9999", "Сбербанк")
        ));
    }
}
