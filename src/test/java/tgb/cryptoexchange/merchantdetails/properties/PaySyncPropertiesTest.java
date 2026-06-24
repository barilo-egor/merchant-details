package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaySyncPropertiesTest {

    @Autowired
    private PaySyncProperties paySyncProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("paysync-url", paySyncProperties.url()),
                () -> assertEquals("paysync-key", paySyncProperties.clientId())
        );
    }
}