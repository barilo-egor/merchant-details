package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RostrastPropertiesTest {

    @Autowired
    private RostrastProperties rostrastProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rostrast-url", rostrastProperties.url()),
                () -> assertEquals("rostrast-key", rostrastProperties.key()),
                () -> assertEquals("rostrast-token", rostrastProperties.token()),
                () -> assertEquals("rostrast-secret", rostrastProperties.secret())
        );
    }
}