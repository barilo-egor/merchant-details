package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StormTradeMerchantCreationServiceTest {

    @InjectMocks
    private StormTradeMerchantCreationService stormTradeMerchantCreationService;

    @Test
    void getMerchantShouldReturnMerchant() {
        assertEquals(Merchant.STORM_TRADE, stormTradeMerchantCreationService.getMerchant());
    }
}