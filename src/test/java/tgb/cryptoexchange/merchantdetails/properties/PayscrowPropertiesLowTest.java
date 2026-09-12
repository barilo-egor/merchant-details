package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayscrowPropertiesLowTest {

    @Autowired
    private PayscrowLowProperties payscrowLowProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-low-url", payscrowLowProperties.url()),
                () -> assertEquals("payscrow-low-key", payscrowLowProperties.key())
        );
    }
}