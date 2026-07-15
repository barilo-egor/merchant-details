package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RsPayBtPropertiesTest {

    @Autowired
    private RSPayBTProperties rsPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rs-pay-bt-url", rsPayProperties.url()),
                () -> assertEquals("rs-pay-bt-api-key", rsPayProperties.apiKey()),
                () -> assertEquals("rs-pay-bt-secret", rsPayProperties.secret())
        );
    }
}