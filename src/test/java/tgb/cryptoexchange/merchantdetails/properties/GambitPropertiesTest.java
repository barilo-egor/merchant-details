package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GambitPropertiesTest {

    @Autowired
    private GambitProperties gambitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("gambit-url", gambitProperties.url()),
                () -> assertEquals("gambit-key", gambitProperties.key()),
                () -> assertEquals("gambit-terminal", gambitProperties.terminal())
        );
    }
}