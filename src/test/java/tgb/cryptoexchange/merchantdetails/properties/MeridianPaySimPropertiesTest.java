package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MeridianPaySimPropertiesTest {

    @Autowired
    private MeridianPaySimProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-sim-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-sim-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-sim-merchant-id", meridianPayProperties.merchantId())
        );
    }
}