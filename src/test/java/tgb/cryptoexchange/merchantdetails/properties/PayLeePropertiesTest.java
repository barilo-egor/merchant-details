package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayLeePropertiesTest {

    @Autowired
    private PayLeeProperties payLeeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-lee-url", payLeeProperties.url()),
                () -> assertEquals("pay-lee-token", payLeeProperties.token()),
                () -> assertEquals("pay-lee-secret", payLeeProperties.secret())
        );
    }
}