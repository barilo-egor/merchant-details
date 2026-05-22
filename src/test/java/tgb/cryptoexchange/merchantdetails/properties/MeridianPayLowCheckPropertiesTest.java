package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MeridianPayLowCheckPropertiesTest {

    @Autowired
    private MeridianPayLowCheckProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-low-check-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-low-check-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-low-check-merchant-id", meridianPayProperties.merchantId())
        );
    }
}