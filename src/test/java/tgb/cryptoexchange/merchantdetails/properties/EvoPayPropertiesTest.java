package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EvoPayPropertiesTest {

    @Autowired
    private EvoPayProperties evoPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("evo-pay-url", evoPayProperties.url()),
                () -> assertEquals("evo-pay-key", evoPayProperties.key()),
                () -> assertEquals("evo-pay-change-key", evoPayProperties.changeKey())
        );
    }
}