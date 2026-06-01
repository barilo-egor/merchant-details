package tgb.cryptoexchange.merchantdetails.details.crocopay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Base51OrderCreationServiceTest {

    @InjectMocks
    private Base51OrderCreationService base51OrderCreationService;

    @Test
    void getMerchantShouldReturnBase51Merchant() {
        assertEquals(Merchant.BASE_51, base51OrderCreationService.getMerchant());
    }

}
