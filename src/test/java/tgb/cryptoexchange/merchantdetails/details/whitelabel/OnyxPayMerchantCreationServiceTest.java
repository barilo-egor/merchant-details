package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OnyxPayMerchantCreationServiceTest {

    @InjectMocks
    private OnyxPayMerchantCreationService onyxPayMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ONYX_PAY, onyxPayMerchantCreationService.getMerchant());
    }
}