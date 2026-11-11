package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyPayHighCheckPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private ManyPayHighCheckProperties manyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("many-pay-high-check-url", manyPayProperties.url()),
                () -> assertEquals("many-pay-high-check-token", manyPayProperties.token())
        );
    }
}