package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

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