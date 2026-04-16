package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AsgardHighCheckPropertiesTest {

    @Autowired
    private AsgardHighCheckProperties asgardHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("asgard-high-check-url", asgardHighCheckProperties.url()),
                () -> assertEquals("asgard-high-check-token", asgardHighCheckProperties.token()),
                () -> assertEquals("asgard-high-check-secret", asgardHighCheckProperties.secret()),
                () -> assertEquals("asgard-high-check-merchant-id", asgardHighCheckProperties.merchantId())
        );
    }
}