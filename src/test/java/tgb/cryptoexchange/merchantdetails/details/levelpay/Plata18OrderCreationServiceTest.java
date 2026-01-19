package tgb.cryptoexchange.merchantdetails.details.levelpay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Plata18OrderCreationServiceTest {

    @InjectMocks
    private Plata18OrderCreationService plata18OrderCreationService;

    @Test
    void getMerchantShouldReturnMobius() {
        assertEquals(Merchant.PLATA_18, plata18OrderCreationService.getMerchant());
    }
}