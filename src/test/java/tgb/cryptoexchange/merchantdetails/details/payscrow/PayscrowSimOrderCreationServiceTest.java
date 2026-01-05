package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PayscrowSimOrderCreationServiceTest {

    @InjectMocks
    private PayscrowSimOrderCreationService service;

    @Test
    void getMerchantShouldReturnPayscrowHighCheck() {
        assertEquals(Merchant.PAYSCROW_SIM, service.getMerchant());
    }
}