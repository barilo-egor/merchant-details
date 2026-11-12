package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.OrderCreationRequest;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnyxPayMerchantCreationServiceTest {

    @Mock
    private OnyxPayProperties onyxPayProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private OnyxPayMerchantCreationService onyxPayMerchantCreationService;

    @Test
    void getMerchantShouldReturnOnyxPay() {
        assertEquals(Merchant.ONYX_PAY, onyxPayMerchantCreationService.getMerchant());
    }

    @Test
    void buildResponseShouldSetBankAndDetailsForCard() {
        Response response = new Response();
        response.setId("onyx-101");
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentOption(Method.TO_CARD);
        dealDTO.setPaymentMethod("sberbank");
        RequisitesDTO req = new RequisitesDTO();
        req.setRequisites("2200111122223333");
        dealDTO.setRequisites(req);
        response.setDeals(List.of(dealDTO));

        Optional<DetailsResponse> result = onyxPayMerchantCreationService.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertAll(
                () -> assertEquals("Сбербанк", details.getBank()),
                () -> assertEquals("2200111122223333", details.getDetails()),
                () -> assertEquals("onyx-101", details.getMerchantOrderId()),
                () -> assertEquals(Merchant.ONYX_PAY, details.getMerchant())
        );
    }

    @Test
    void buildResponseShouldClearBankAndDetailsForCrossBorder() {
        Response response = new Response();
        response.setId("onyx-102");
        DealDTO dealDTO = new DealDTO();
        dealDTO.setPaymentOption(Method.CROSS_BORDER);
        dealDTO.setPaymentMethod("sberbank");
        dealDTO.setQrCodeLink("https://qr.nspk.ru/cross");
        RequisitesDTO req = new RequisitesDTO();
        req.setRequisites("2200111122223333");
        dealDTO.setRequisites(req);
        response.setDeals(List.of(dealDTO));

        Optional<DetailsResponse> result = onyxPayMerchantCreationService.buildResponse(response);
        assertTrue(result.isPresent());
        DetailsResponse details = result.get();
        assertAll(
                () -> assertNull(details.getBank()),
                () -> assertNull(details.getDetails()),
                () -> assertEquals("https://qr.nspk.ru/cross", details.getQr()),
                () -> assertEquals("onyx-102", details.getMerchantOrderId()),
                () -> assertEquals(Merchant.ONYX_PAY, details.getMerchant())
        );
    }

    @ValueSource(strings = {
            "9azdZ2NPS4j8CzV", "2Sl5l0yo0pBdDIJ"
    })
    @ParameterizedTest
    void keyFunctionShouldReturnSimKeyForMobileTopUp(String key) {
        when(onyxPayProperties.simKey()).thenReturn(key);
        assertEquals(key, onyxPayMerchantCreationService.keyFunction().apply(Method.MOBILE_TOP_UP));
    }

    @CsvSource(textBlock = """
            9azdZ2NPS4j8CzV,TO_CARD
            2Sl5l0yo0pBdDIJ,SBP
            """)
    @ParameterizedTest
    void keyFunctionShouldReturnKeyForNotMobileTopUp(String key, Method method) {
        when(onyxPayProperties.key()).thenReturn(key);
        assertEquals(key, onyxPayMerchantCreationService.keyFunction().apply(method));
    }

    @CsvSource({
            "https://gateway.paysendmmm.online,b2DVpRm6WXxzBvN",
            "https://bulba.paysendmmm.online,gCQ8DmeRRWb5fVm"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(String gatewayUrl, String secret) {
        OrderCreationRequest detailsRequest = new OrderCreationRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod(Method.SBP.name());
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(onyxPayProperties.token()).thenReturn("token");

        Request actual = onyxPayMerchantCreationService.body(detailsRequest);
        assertEquals(gatewayUrl + "/merchant-details/callback?merchant=ONYX_PAY&secret=" + secret,
                actual.getNotificationUrl());
    }
}