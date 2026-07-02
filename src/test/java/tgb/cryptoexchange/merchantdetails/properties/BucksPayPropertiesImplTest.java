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
                () -> assertEquals("bucks-pay-qr-key", bucksPayProperties.qrKey()),
                () -> assertEquals("bucks-pay-t-pay-key", bucksPayProperties.tPayKey()),
                () -> assertEquals("bucks-pay-secret", bucksPayProperties.secret()),
                () -> assertEquals("bucks-pay-qr-secret", bucksPayProperties.qrSecret()),
                () -> assertEquals("bucks-pay-t-pay-secret", bucksPayProperties.tPaySecret()),
                () -> assertEquals("bucks-pay-shop-id", bucksPayProperties.shopId()),
                () -> assertEquals("bucks-pay-qr-shop-id", bucksPayProperties.qrShopId()),
                () -> assertEquals("bucks-pay-t-pay-shop-id", bucksPayProperties.tPayShopId())
        );
    }
}