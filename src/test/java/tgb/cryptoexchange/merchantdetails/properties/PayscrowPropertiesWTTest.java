package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayscrowPropertiesWTTest {

    @Autowired
    private PayscrowWTProperties payscrowWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-white-triangle-url", payscrowWTProperties.url()),
                () -> assertEquals("payscrow-white-triangle-key", payscrowWTProperties.key())
        );
    }
}