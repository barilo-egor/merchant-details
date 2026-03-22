package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class YoloPropertiesImplTest {

    @Autowired
    private YoloPropertiesImpl yoloProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("yolo-url", yoloProperties.url()),
                () -> assertEquals("yolo-account-id", yoloProperties.accountId()),
                () -> assertEquals("yolo-store-key", yoloProperties.storeKey()),
                () -> assertEquals("yolo-jwt-login", yoloProperties.credentials().login()),
                () -> assertEquals("yolo-jwt-passphrase", yoloProperties.credentials().passphrase())
        );
    }
}