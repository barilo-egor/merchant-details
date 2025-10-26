package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeoTransferPropertiesTest {

    @Autowired
    private GeoTransferProperties geoTransferProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("geo-transfer-url", geoTransferProperties.url()),
                () -> assertEquals("geo-transfer-key", geoTransferProperties.key()),
                () -> assertEquals("geo-transfer-token", geoTransferProperties.token()),
                () -> assertEquals("geo-transfer-secret", geoTransferProperties.secret())
        );
    }
}