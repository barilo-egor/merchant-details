package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StormTradePropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private StormTradeProperties stormTradeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("storm-trade-url", stormTradeProperties.url()),
                () -> assertEquals("storm-trade-key", stormTradeProperties.key()),
                () -> assertEquals("storm-trade-token", stormTradeProperties.token()),
                () -> assertEquals("storm-trade-secret", stormTradeProperties.secret())
        );
    }
}