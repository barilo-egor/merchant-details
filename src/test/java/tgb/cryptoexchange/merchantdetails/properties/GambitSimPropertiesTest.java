package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GambitSimPropertiesTest {

    @Autowired
    private GambitSimProperties gambitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("gambit-sim-url", gambitProperties.url()),
                () -> assertEquals("gambit-sim-key", gambitProperties.key()),
                () -> assertEquals("gambit-sim-terminal", gambitProperties.terminal())
        );
    }
}