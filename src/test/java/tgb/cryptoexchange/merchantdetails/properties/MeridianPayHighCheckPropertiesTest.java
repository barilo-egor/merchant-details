package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MeridianPayHighCheckPropertiesTest {

    @Autowired
    private MeridianPayHighCheckProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-high-check-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-high-check-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-high-check-merchant-id", meridianPayProperties.merchantId())
        );
    }
}