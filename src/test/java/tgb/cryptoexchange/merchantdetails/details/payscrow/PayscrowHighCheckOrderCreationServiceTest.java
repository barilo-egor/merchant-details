package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PayscrowHighCheckOrderCreationServiceTest {

    @InjectMocks
    private PayscrowHighCheckOrderCreationService service;

    @Test
    void getMerchantShouldReturnPayscrowHighCheck() {
        assertEquals(Merchant.PAYSCROW_HIGH_CHECK, service.getMerchant());
    }
}