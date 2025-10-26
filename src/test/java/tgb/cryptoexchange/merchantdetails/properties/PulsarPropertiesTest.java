package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PulsarPropertiesTest {

    @Autowired
    private PulsarProperties pulsarProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pulsar-url", pulsarProperties.url()),
                () -> assertEquals("pulsar-token", pulsarProperties.token()),
                () -> assertEquals("pulsar-code", pulsarProperties.code()),
                () -> assertEquals("pulsar-secret", pulsarProperties.secret())
        );
    }
}