package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class YaPayOrderCreationServiceTest {

    @InjectMocks
    private YaPayOrderCreationService yaPayOrderCreationService;

    @Test
    void getMerchantShouldReturnYaPay() {
        assertEquals(Merchant.YA_PAY, yaPayOrderCreationService.getMerchant());
    }
}