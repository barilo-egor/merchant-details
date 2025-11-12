package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SettleXPropertiesTest {

    @Autowired
    private SettleXProperties settleXProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("settle-x-url", settleXProperties.url()),
                () -> assertEquals("settle-x-key", settleXProperties.key())
        );
    }
}