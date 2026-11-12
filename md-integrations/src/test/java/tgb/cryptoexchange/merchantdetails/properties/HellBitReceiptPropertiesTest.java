package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HellBitReceiptPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private HellBitReceiptProperties hellBitReceiptProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("hell-bit-receipt-token", hellBitReceiptProperties.token()),
                () -> assertEquals("hell-bit-receipt-sign-key", hellBitReceiptProperties.signKey())
        );
    }
}
