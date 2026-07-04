package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Base51PropertiesTest {

    @Autowired
    private Base51Properties base51Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("base51-url", base51Properties.url()),
                () -> assertEquals("base51-client-id", base51Properties.clientId()),
                () -> assertEquals("base51-client-secret", base51Properties.clientSecret())
        );
    }
}