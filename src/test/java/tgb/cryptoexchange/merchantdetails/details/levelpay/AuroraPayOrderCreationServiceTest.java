package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuroraPayOrderCreationServiceTest {

    @InjectMocks
    private AuroraPayOrderCreationService auroraPayOrderCreationService;

    @Test
    void getMerchantShouldReturnMobius() {
        assertEquals(Merchant.AURORA_PAY, auroraPayOrderCreationService.getMerchant());
    }
}