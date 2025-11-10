package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OnyxPaySimPropertiesTest {

    @Autowired
    private OnyxPaySimProperties onyxPaySimProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("onyx-pay-sim-url", onyxPaySimProperties.url()),
                () -> assertEquals("onyx-pay-sim-key", onyxPaySimProperties.key()),
                () -> assertEquals("onyx-pay-sim-token", onyxPaySimProperties.token()),
                () -> assertEquals("onyx-pay-sim-secret", onyxPaySimProperties.secret())
        );
    }
}