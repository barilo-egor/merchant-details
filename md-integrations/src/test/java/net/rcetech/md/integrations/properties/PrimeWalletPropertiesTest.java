package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimeWalletPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PrimeWalletProperties primeWalletProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("prime-wallet-url", primeWalletProperties.url()),
                () -> assertEquals("prime-wallet-token", primeWalletProperties.token())
        );
    }
}