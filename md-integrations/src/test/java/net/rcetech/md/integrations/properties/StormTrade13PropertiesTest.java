package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StormTrade13PropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private StormTrade13Properties stormTrade13Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("storm-trade13-url", stormTrade13Properties.url()),
                () -> assertEquals("storm-trade13-key", stormTrade13Properties.key()),
                () -> assertEquals("storm-trade13-token", stormTrade13Properties.token()),
                () -> assertEquals("storm-trade13-secret", stormTrade13Properties.secret())
        );
    }
}