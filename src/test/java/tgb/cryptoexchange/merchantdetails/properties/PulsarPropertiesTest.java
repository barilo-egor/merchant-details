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
                () -> assertEquals("pulsar-key", pulsarProperties.token()),
                () -> assertEquals("pulsar-key", pulsarProperties.code()),
                () -> assertEquals("pulsar-key", pulsarProperties.secret())
        );
    }
}