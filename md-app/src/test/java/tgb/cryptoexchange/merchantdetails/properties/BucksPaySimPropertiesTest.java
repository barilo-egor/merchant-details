package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BucksPaySimPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private BucksPaySimProperties bucksPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bucks-pay-sim-url", bucksPayProperties.url()),
                () -> assertEquals("bucks-pay-sim-key", bucksPayProperties.key()),
                () -> assertEquals("bucks-pay-sim-secret", bucksPayProperties.secret()),
                () -> assertEquals("bucks-pay-sim-shop-id", bucksPayProperties.shopId())
        );
    }
}