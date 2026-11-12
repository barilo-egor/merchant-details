package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimeWalletLowCheckPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private PrimeWalletLowCheckProperties primeWalletProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("prime-wallet-low-check-url", primeWalletProperties.url()),
                () -> assertEquals("prime-wallet-low-check-token", primeWalletProperties.token())
        );
    }
}