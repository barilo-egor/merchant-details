package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.OnyxPayProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnyxPayMerchantCreationServiceTest {

    @Mock
    private OnyxPayProperties onyxPayProperties;

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
}