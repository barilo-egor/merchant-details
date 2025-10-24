package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PayPointsPropertiesTest {

    @Autowired
    private PayPointsProperties payPointsProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-points-url", payPointsProperties.url()),
                () -> assertEquals("pay-points-key", payPointsProperties.token())
        );
    }
}