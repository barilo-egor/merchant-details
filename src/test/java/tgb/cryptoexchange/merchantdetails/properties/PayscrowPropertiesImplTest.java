package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayscrowPropertiesImplTest {

    @Autowired
    private PayscrowPropertiesImpl payscrowPropertiesImpl;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-url", payscrowPropertiesImpl.url()),
                () -> assertEquals("payscrow-key", payscrowPropertiesImpl.key()),
                () -> assertEquals("payscrow-high-check-key", payscrowPropertiesImpl.highCheckKey()),
                () -> assertEquals("payscrow-in-house-key", payscrowPropertiesImpl.inHouseKey()),
                () -> assertEquals("payscrow-white-triangle-key", payscrowPropertiesImpl.whiteTriangleKey())
        );
    }
}