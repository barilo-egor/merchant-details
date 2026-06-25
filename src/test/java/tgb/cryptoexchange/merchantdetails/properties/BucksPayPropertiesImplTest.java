package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BucksPayPropertiesImplTest {

    @Autowired
    private BucksPayPropertiesImpl bucksPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bucks-pay-url", bucksPayProperties.url()),
                () -> assertEquals("bucks-pay-key", bucksPayProperties.key()),
                () -> assertEquals("bucks-pay-secret", bucksPayProperties.secret()),
                () -> assertEquals("bucks-pay-shop-id", bucksPayProperties.shopId())
        );
    }
}