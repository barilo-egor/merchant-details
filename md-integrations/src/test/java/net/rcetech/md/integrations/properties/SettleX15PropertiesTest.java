package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SettleX15PropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private SettleX15Properties settleXProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("settle-x15-url", settleXProperties.url()),
                () -> assertEquals("settle-x15-key", settleXProperties.key()),
                () -> assertEquals("settle-sbp-id", settleXProperties.sbpId()),
                () -> assertEquals("settle-c2c-id", settleXProperties.c2cId())
        );
    }
}