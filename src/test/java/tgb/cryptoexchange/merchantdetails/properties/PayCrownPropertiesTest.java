package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayCrownPropertiesTest {

    @Autowired
    private PayCrownProperties payCrownProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-crown-key", payCrownProperties.key()),
                () -> assertEquals("pay-crown-url", payCrownProperties.url()),
                () -> assertEquals("pay-crown-secret", payCrownProperties.secret()),
                () -> assertEquals("pay-crown-merchant-id", payCrownProperties.merchantId())
        );
    }
}