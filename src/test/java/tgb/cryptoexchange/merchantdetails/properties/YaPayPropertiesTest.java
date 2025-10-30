package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class YaPayPropertiesTest {

    @Autowired
    private YaPayProperties yaPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-box-url", yaPayProperties.url()),
                () -> assertEquals("pay-box-token", yaPayProperties.token()),
                () -> assertEquals("pay-box-sign-key", yaPayProperties.signKey())
        );
    }
}