package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyPayLowCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ManyPayLowCheckProperties manyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("many-pay-low-check-url", manyPayProperties.url()),
                () -> assertEquals("many-pay-low-check-token", manyPayProperties.token())
        );
    }
}