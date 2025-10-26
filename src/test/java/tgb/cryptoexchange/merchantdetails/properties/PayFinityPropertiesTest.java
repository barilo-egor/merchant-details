package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayFinityPropertiesTest {

    @Autowired
    private PayFinityProperties payFinityProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-finity-url", payFinityProperties.url()),
                () -> assertEquals("pay-finity-keys-private-key", payFinityProperties.keys().privateKey()),
                () -> assertEquals("pay-finity-keys-public-key", payFinityProperties.keys().publicKey()),
                () -> assertEquals(10, payFinityProperties.timeout())
        );
    }
}