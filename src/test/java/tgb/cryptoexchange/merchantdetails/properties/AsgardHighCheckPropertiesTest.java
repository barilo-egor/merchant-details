package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AsgardHighCheckPropertiesTest extends AbstractMySqlTest {

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