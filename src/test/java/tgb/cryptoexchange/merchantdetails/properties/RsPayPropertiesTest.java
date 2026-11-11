package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RsPayPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private RSPayImplProperties rsPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rs-pay-url", rsPayProperties.url()),
                () -> assertEquals("rs-pay-api-key", rsPayProperties.apiKey()),
                () -> assertEquals("rs-pay-secret", rsPayProperties.secret())
        );
    }
}