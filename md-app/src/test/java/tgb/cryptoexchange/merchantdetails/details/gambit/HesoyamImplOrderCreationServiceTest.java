package tgb.cryptoexchange.merchantdetails.details.gambit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HesoyamImplOrderCreationServiceTest {

    @InjectMocks
    private HesoyamImplOrderCreationService service;

    @Test
    void getMerchantShouldReturnGambit() {
        assertEquals(Merchant.HESOYAM, service.getMerchant());
    }

}