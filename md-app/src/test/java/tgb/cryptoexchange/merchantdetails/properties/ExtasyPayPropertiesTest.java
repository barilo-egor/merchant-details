package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtasyPayPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private ExtasyPayProperties extasyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("extasy-pay-url", extasyPayProperties.url()),
                () -> assertEquals("extasy-pay-token", extasyPayProperties.token()),
                () -> assertEquals("extasy-pay-internal-qr-token", extasyPayProperties.internalQrToken())
        );
    }
}