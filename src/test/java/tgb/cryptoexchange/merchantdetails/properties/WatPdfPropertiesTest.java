package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WatPdfPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private WatPdfProperties watProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wat-pdf-token", watProperties.token())
        );
    }
}