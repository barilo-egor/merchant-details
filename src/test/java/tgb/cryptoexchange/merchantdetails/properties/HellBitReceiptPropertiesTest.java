package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HellBitReceiptPropertiesTest {

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
