package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Way2PayPropertiesTest {

    @Autowired
    private Way2PayProperties way2PayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("way2pay-url", way2PayProperties.url()),
                () -> assertEquals("way2pay-environment", way2PayProperties.environment()),
                () -> assertEquals("way2pay-secret", way2PayProperties.secret()),
                () -> assertEquals("way2pay-pub", way2PayProperties.pub())
        );
    }
}