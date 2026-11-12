package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HoneyMoneyPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private HoneyMoneyProperties honeyMoneyProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("honey-money-url", honeyMoneyProperties.url()),
                () -> assertEquals("honey-money-client-id", honeyMoneyProperties.clientId()),
                () -> assertEquals("honey-money-auth-token", honeyMoneyProperties.authToken()),
                () -> assertEquals("honey-money-sign-token", honeyMoneyProperties.signToken())
        );
    }
}