package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PayBoxPropertiesTest {

    @Autowired
    private PayBoxProperties payBoxProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-box-url", payBoxProperties.url()),
                () -> assertEquals("pay-box-token", payBoxProperties.token()),
                () -> assertEquals("pay-box-sign-key", payBoxProperties.signKey())
        );
    }
}