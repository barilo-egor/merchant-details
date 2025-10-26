package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NicePayPropertiesTest {

    @Autowired
    private NicePayProperties nicePayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("nice-pay-url", nicePayProperties.url()),
                () -> assertEquals("nice-pay-merchant-id", nicePayProperties.merchantId()),
                () -> assertEquals("nice-pay-secret", nicePayProperties.secret())
        );
    }
}