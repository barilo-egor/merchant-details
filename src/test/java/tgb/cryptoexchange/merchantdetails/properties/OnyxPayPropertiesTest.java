package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OnyxPayPropertiesTest {

    @Autowired
    private OnyxPayProperties onyxPayPropeties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("onyx-pay-url", onyxPayPropeties.url()),
                () -> assertEquals("onyx-pay-key", onyxPayPropeties.key()),
                () -> assertEquals("onyx-pay-token", onyxPayPropeties.token()),
                () -> assertEquals("onyx-pay-secret", onyxPayPropeties.secret())
        );
    }
}