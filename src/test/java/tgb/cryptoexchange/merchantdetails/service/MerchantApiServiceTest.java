package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class MerchantApiServiceTest {

    private final MerchantApiService merchantApiService = new MerchantApiService();

    @ValueSource(strings = {"0.10.0", "0.10.1", "1.0.0", "0.11.0"})
    @ParameterizedTest
    void getMerchantsByApiVersionShouldReturnAllMerchantsIfVersionMoreThanOrEqualTo0100(String version) {
        assertEquals(Arrays.asList(Merchant.values()), merchantApiService.getMerchantsByApiVersion(version));
    }


    @ValueSource(strings = {"0.9.9", "0.9.0", "0.0.1", "0.0", "0", "qwe", "0.1", "0.10"})
    @ParameterizedTest
    void getMerchantsByApiVersionShouldReturnAllExceptPlataMerchantsIfVersionLessThan0100OrInvalid(String version) {
        List<Merchant> actual = merchantApiService.getMerchantsByApiVersion(version);
        assertEquals(Arrays.asList(Merchant.values()).size() - 1, actual.size());
        assertFalse(actual.contains(Merchant.PLATA_PAYMENT));
    }
}