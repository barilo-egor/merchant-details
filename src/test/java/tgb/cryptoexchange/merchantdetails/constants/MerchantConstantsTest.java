package tgb.cryptoexchange.merchantdetails.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.*;

class MerchantConstantsTest {

    @ParameterizedTest(name = "[{0}] Statuses: No Throw")
    @EnumSource(Merchant.class)
    void checkStatusesDoesNotThrow(Merchant merchant) {
        assertDoesNotThrow(() -> MerchantConstants.getStatuses(merchant));
    }

    @ParameterizedTest(name = "[{0}] Statuses: Not Null")
    @EnumSource(Merchant.class)
    void checkStatusesNotNull(Merchant merchant) {
        assertNotNull(MerchantConstants.getStatuses(merchant));
    }

    @ParameterizedTest(name = "[{0}] Methods: Not Null")
    @EnumSource(Merchant.class)
    void checkMethodsNotNull(Merchant merchant) {
        assertNotNull(MerchantConstants.getMethods(merchant));
    }

    @ParameterizedTest(name = "[{0}] Statuses: Size > 0")
    @EnumSource(Merchant.class)
    void checkStatusesNotEmpty(Merchant merchant) {
        assertNotEquals(0, MerchantConstants.getStatuses(merchant).size());
    }

    @ParameterizedTest(name = "[{0}] Methods: Size > 0")
    @EnumSource(Merchant.class)
    void checkMethodsNotEmpty(Merchant merchant) {
        assertNotEquals(0, MerchantConstants.getMethods(merchant).size());
    }
}