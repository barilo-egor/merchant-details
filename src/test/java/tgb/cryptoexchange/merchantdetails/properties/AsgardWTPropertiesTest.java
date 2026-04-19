package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AsgardWTPropertiesTest {

    @Autowired
    private AsgardWTProperties asgardWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("asgard-wt-url", asgardWTProperties.url()),
                () -> assertEquals("asgard-wt-token", asgardWTProperties.token()),
                () -> assertEquals("asgard-wt-secret", asgardWTProperties.secret()),
                () -> assertEquals("asgard-wt-merchant-id", asgardWTProperties.merchantId())
        );
    }
}