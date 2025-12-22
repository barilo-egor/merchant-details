package tgb.cryptoexchange.merchantdetails.constants;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.*;

class MerchantConstantsTest {

    @Test
    void allMerchantsMethodsAndStatusesShouldPresent() {
        for (Merchant merchant: Merchant.values()) {
            assertDoesNotThrow(() -> MerchantConstants.getStatuses(merchant));
            assertNotNull(MerchantConstants.getStatuses(merchant));
            assertNotNull(MerchantConstants.getMethods(merchant));
            assertNotEquals(0, MerchantConstants.getStatuses(merchant).size());
            assertNotEquals(0, MerchantConstants.getMethods(merchant).size());
        }
    }

}