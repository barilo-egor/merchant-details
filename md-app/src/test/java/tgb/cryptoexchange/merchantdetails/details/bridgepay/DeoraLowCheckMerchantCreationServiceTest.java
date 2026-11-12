package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DeoraLowCheckMerchantCreationServiceTest {

    @InjectMocks
    private DeoraLowCheckMerchantCreationService deoraMerchantCreationService;

    @Test
    void getMerchantShouldReturnDeora() {
        assertEquals(Merchant.DEORA_LOW_CHECK, deoraMerchantCreationService.getMerchant());
    }
}