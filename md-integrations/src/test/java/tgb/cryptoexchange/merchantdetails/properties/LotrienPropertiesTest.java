package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LotrienPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private LotrienProperties lotrienProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("lotrien-url", lotrienProperties.url()),
                () -> assertEquals("lotrien-key", lotrienProperties.key())
        );
    }
}