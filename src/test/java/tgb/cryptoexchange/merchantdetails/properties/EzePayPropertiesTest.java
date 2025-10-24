package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EzePayPropertiesTest {

    @Autowired
    private EzePayProperties ezePayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("eze-pay-url", ezePayProperties.url()),
                () -> assertEquals("eze-pay-key", ezePayProperties.key()),
                () -> assertEquals("eze-pay-id", ezePayProperties.id())
        );
    }
}