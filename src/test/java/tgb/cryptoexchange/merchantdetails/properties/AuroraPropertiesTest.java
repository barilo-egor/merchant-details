package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AuroraPropertiesTest {

    @Autowired
    private AuroraPayProperties auroraPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("aurora-url", auroraPayProperties.url()),
                () -> assertEquals("aurora-token", auroraPayProperties.token()),
                () -> assertEquals("aurora-merchant-id", auroraPayProperties.merchantId())
        );
    }
}