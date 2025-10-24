package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MobiusPropertiesTest {

    @Autowired
    private MobiusProperties mobiusProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("mobius-url", mobiusProperties.url()),
                () -> assertEquals("mobius-token", mobiusProperties.token()),
                () -> assertEquals("mobius-merchant-id", mobiusProperties.merchantId())
        );
    }
}