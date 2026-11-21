package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoTransferPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private GeoTransferProperties geoTransferProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("geo-transfer-url", geoTransferProperties.url()),
                () -> assertEquals("geo-transfer-key", geoTransferProperties.key()),
                () -> assertEquals("geo-transfer-token", geoTransferProperties.token()),
                () -> assertEquals("geo-transfer-secret", geoTransferProperties.secret())
        );
    }
}