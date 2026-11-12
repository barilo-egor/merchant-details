package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OnyxPayPropertiesTest extends AbstractIntegrationsContextTest {

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