package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LotrienPropertiesTest extends AbstractMySqlTest {

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