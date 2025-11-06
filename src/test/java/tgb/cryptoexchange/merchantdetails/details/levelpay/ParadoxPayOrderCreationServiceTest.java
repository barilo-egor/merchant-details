package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ParadoxPayOrderCreationServiceTest {

    @InjectMocks
    private ParadoxPayOrderCreationService paradoxPayOrderCreationService;

    @Test
    void getMerchantShouldReturnMobius() {
        assertEquals(Merchant.PARADOX_PAY, paradoxPayOrderCreationService.getMerchant());
    }
}