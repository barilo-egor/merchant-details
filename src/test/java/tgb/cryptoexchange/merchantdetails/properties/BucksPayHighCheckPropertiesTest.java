package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BucksPayHighCheckPropertiesTest {

    @Autowired
    private BucksPayHighCheckProperties bucksPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bucks-pay-high-check-url", bucksPayProperties.url()),
                () -> assertEquals("bucks-pay-high-check-key", bucksPayProperties.key()),
                () -> assertEquals("bucks-pay-high-check-secret", bucksPayProperties.secret()),
                () -> assertEquals("bucks-pay-high-check-shop-id", bucksPayProperties.shopId())
        );
    }
}