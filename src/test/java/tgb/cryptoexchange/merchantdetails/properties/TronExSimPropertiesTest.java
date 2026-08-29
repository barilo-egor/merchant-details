package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TronExSimPropertiesTest {

    @Autowired
    private TronExSimProperties tronExProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tron-ex-sim-url", tronExProperties.url()),
                () -> assertEquals("tron-ex-sim-key", tronExProperties.key())
        );
    }
}