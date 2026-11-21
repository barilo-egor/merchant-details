package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YoloPropertiesImplTest extends AbstractIntegrationsContextTest {

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