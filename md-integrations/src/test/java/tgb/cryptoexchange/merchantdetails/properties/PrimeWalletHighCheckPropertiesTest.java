package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimeWalletHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PrimeWalletHighCheckProperties primeWalletProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("prime-wallet-high-check-url", primeWalletProperties.url()),
                () -> assertEquals("prime-wallet-high-check-token", primeWalletProperties.token())
        );
    }
}