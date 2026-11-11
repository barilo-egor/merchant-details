package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Base51LowCheckPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private Base51LowCheckProperties base51Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("base51-low-check-url", base51Properties.url()),
                () -> assertEquals("base51-low-check-client-id", base51Properties.clientId()),
                () -> assertEquals("base51-low-check-client-secret", base51Properties.clientSecret())
        );
    }
}