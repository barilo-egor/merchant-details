package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MeridianPayNspkPropertiesTest {

    @Autowired
    private MeridianPayNspkProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-nspk-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-nspk-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-nspk-merchant-id", meridianPayProperties.merchantId())
        );
    }
}