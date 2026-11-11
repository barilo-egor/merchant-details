package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SouzPdfCreationServiceTest {

    @InjectMocks
    private SouzPdfMerchantCreationService souzPdfMerchantCreationService;

    @Test
    void getMerchantShouldReturnSouzPdf() {
        assertEquals(Merchant.SOUZ_PDF, souzPdfMerchantCreationService.getMerchant());
    }

}