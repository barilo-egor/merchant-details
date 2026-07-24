package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LuckyPayPropertiesTest {

    @Autowired
    private LuckyPayProperties luckyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("lucky-pay-url", luckyPayProperties.url()),
                () -> assertEquals("lucky-pay-key", luckyPayProperties.key())
        );
    }
}