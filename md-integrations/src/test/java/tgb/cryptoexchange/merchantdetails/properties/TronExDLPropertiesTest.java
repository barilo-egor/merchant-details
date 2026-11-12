package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TronExDLPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private TronExDLProperties tronExProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tron-ex-dl-key", tronExProperties.key()),
                () -> assertEquals("tron-ex-dl-dl-url", tronExProperties.dlUrl())
        );
    }
}