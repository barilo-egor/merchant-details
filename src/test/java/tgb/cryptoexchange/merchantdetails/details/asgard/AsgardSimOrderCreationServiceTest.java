package tgb.cryptoexchange.merchantdetails.details.asgard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AsgardSimOrderCreationServiceTest {


    @InjectMocks
    private AsgardSimOrderCreationService service;

    @Test
    void getMerchantShouldReturnGambit() {
        assertEquals(Merchant.ASGARD_SIM, service.getMerchant());
    }

}