package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ZPayPropertiesImplTest {

    @Autowired
    private ZPayProperties zPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("z-pay-url", zPayProperties.url()),
                () -> assertEquals("z-pay-token", zPayProperties.token())
        );
    }
}