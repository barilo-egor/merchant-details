package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParadoxPayPropertiesTest {

    @Autowired
    private ParadoxPayProperties paradoxPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("paradox-pay-url", paradoxPayProperties.url()),
                () -> assertEquals("paradox-pay-merchant-id", paradoxPayProperties.merchantId()),
                () -> assertEquals("paradox-pay-token", paradoxPayProperties.token())
        );
    }
}