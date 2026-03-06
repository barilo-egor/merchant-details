package tgb.cryptoexchange.merchantdetails.constants;

import org.junit.jupiter.api.Test;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.*;

class MerchantConstantsTest {

    @Test
    void allMerchantsMethodsAndStatusesShouldPresent() {
        for (Merchant merchant: Merchant.values()) {
            assertDoesNotThrow(() -> MerchantConstants.getStatuses(merchant),
                    "For merchant " + merchant + " getStatuses throws exception.");
            assertNotNull(MerchantConstants.getStatuses(merchant),
                    "For merchant " + merchant + " getStatuses is null.");
            assertNotNull(MerchantConstants.getMethods(merchant),
                    "For merchant " + merchant + " getMethods is null.");
            assertNotEquals(0, MerchantConstants.getStatuses(merchant).size(),
                    "For merchant " + merchant + " getStatuses has size 0.");
            assertNotEquals(0, MerchantConstants.getMethods(merchant).size(),
                    "For merchant " + merchant + " getMethods has size 0.");
        }
    }

}