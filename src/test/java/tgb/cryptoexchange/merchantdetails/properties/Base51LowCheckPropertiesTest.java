package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Base51LowCheckPropertiesTest {

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