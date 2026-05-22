package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MeridianPayPropertiesTest {

    @Autowired
    private MeridianPayProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-merchant-id", meridianPayProperties.merchantId())
        );
    }
}