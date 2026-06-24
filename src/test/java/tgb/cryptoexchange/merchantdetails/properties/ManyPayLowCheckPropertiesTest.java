package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ManyPayLowCheckPropertiesTest {

    @Autowired
    private ManyPayLowCheckProperties manyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("many-pay-low-check-url", manyPayProperties.url()),
                () -> assertEquals("many-pay-low-check-token", manyPayProperties.token())
        );
    }
}