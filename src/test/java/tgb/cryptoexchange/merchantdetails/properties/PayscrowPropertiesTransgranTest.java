package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayscrowPropertiesTransgranTest {

    @Autowired
    private PayscrowTransgranProperties payscrowTransgranProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-transgran-url", payscrowTransgranProperties.url()),
                () -> assertEquals("payscrow-transgran-key", payscrowTransgranProperties.key())
        );
    }
}