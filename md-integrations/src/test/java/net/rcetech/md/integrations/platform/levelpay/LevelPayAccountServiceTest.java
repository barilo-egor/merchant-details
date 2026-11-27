package net.rcetech.md.integrations.platform.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.config.CallbackConfig;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.meta.merchant.MerchantCatalog;
import net.rcetech.md.integrations.properties.LevelPayProperties;
import net.rcetech.md.integrations.service.SleepingService;
import net.rcetech.md.integrations.service.SignatureService;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Общий тест аккаунтов levelpay. Мерчанты с флагом amountMinorUnits в справочнике
 * получают сумму от площадки в копейках — ожидаемая сумма в тесте считается из флага.
 */
@ExtendWith(MockitoExtension.class)
class LevelPayAccountServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private LevelPayProperties properties;

    @Mock
    private CallbackConfig callbackConfig;

    @Mock
    private SignatureService signatureService;

    @Mock
    private SleepingService sleepingService;

    static final String[] ACCOUNTS = {"MERIDIAN_PAY", "MERIDIAN_PAY_HIGH_CHECK", "MERIDIAN_PAY_LOW_CHECK",
            "MERIDIAN_PAY_SIM", "MERIDIAN_PAY_NSPK", "PLATA_18", "PLATA_PAYMENT"};

    static Stream<Merchant> accounts() {
        return java.util.Arrays.stream(ACCOUNTS).map(Merchant::valueOf);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {
            "MERIDIAN_PAY", "MERIDIAN_PAY_HIGH_CHECK", "MERIDIAN_PAY_LOW_CHECK",
            "MERIDIAN_PAY_SIM", "MERIDIAN_PAY_NSPK", "PLATA_18", "PLATA_PAYMENT"})
    void getMerchantShouldReturnMerchantFromConstructor(Merchant merchant) {
        assertEquals(merchant, service(merchant).getMerchant());
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.INCLUDE, names = {
            "MERIDIAN_PAY", "MERIDIAN_PAY_HIGH_CHECK", "MERIDIAN_PAY_LOW_CHECK",
            "MERIDIAN_PAY_SIM", "MERIDIAN_PAY_NSPK", "PLATA_18", "PLATA_PAYMENT"})
    void uriBuilderShouldSetPath(Merchant merchant) {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/h2h/order", service(merchant).uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ParameterizedTest
    @MethodSource("merchantWithResponseArgs")
    void buildResponseShouldMapAmountAccordingToCatalogFlag(Merchant merchant, String amount, String gateway, String detail) {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId("lp-order-1");
        order.setStatus(Status.PENDING);
        order.setAmount(amount);
        order.setPaymentGatewayName(gateway);
        Response.Order.PaymentDetail paymentDetail = new Response.Order.PaymentDetail();
        paymentDetail.setDetail(detail);
        order.setPaymentDetail(paymentDetail);
        response.setData(order);

        Optional<DetailsResponse> result = service(merchant).buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse actual = result.get();
        int expectedAmount = Integer.parseInt(amount) / (MerchantCatalog.of(merchant).amountMinorUnits() ? 100 : 1);
        assertAll(
                () -> assertEquals(gateway, actual.getBank()),
                () -> assertEquals(detail, actual.getDetails()),
                () -> assertEquals(expectedAmount, actual.getAmount()),
                () -> assertEquals(merchant, actual.getMerchant())
        );
    }

    @Test
    void buildResponseShouldSetQrWhenDetailIsNull() {
        Response response = new Response();
        Response.Order order = new Response.Order();
        order.setOrderId("lp-order-3");
        order.setStatus(Status.PENDING);
        order.setAmount("2000");

        Response.Order.PaymentDetail detail = new Response.Order.PaymentDetail();
        detail.setQrCodeLink("https://qr.example.com/pay");
        order.setPaymentDetail(detail);
        response.setData(order);

        Optional<DetailsResponse> result = service(Merchant.PLATA_18).buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse detailsResponse = result.get();
        assertAll(
                () -> assertNull(detailsResponse.getBank()),
                () -> assertNull(detailsResponse.getDetails()),
                () -> assertEquals("https://qr.example.com/pay", detailsResponse.getQr())
        );
    }

    static Stream<Arguments> merchantWithResponseArgs() {
        return accounts().flatMap(m -> Stream.of(
                Arguments.of(m, "5000", "T-Bank", "+79991234567"),
                Arguments.of(m, "3000", "SBER", "4276123412341234")));
    }

    private LevelPayOrderCreationService service(Merchant merchant) {
        return new LevelPayOrderCreationService(merchant, restClient, callbackConfig, properties);
    }
}
