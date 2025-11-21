package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ONYX_PAY, onyxPayMerchantCreationService.getMerchant());
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
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethod("SBP");
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(onyxPayProperties.token()).thenReturn("token");

        Request actual = onyxPayMerchantCreationService.body(detailsRequest);
        assertEquals(gatewayUrl + "/merchant-details/callback?merchant=ONYX_PAY&secret=" + secret,
                actual.getNotificationUrl());
    }
}