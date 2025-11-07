package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LuckyPayOrderCreationServiceTest {

    @InjectMocks
    private LuckyPayOrderCreationService service;

    @Test
    void getMerchantShouldReturnLuckyPay() {
        assertEquals(Merchant.LUCKY_PAY, service.getMerchant());
    }
}