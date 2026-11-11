package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TronExPdfPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private TronExPdfProperties tronExProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tron-ex-pdf-key", tronExProperties.key())
        );
    }
}