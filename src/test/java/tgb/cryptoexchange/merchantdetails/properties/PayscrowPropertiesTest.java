package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PayscrowPropertiesTest {

    @Autowired
    private PayscrowProperties payscrowProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-url", payscrowProperties.url()),
                () -> assertEquals("payscrow-key", payscrowProperties.key())
        );
    }
}