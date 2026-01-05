package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayscrowHighCheckPropertiesTest {

    @Autowired
    private PayscrowHighCheckProperties payscrowHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-high-check-url", payscrowHighCheckProperties.url()),
                () -> assertEquals("payscrow-high-check-key", payscrowHighCheckProperties.key())
        );
    }
}