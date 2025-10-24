package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PspWarePropertiesTest {

    @Autowired
    private PspWareProperties pspWareProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("psp-ware-url", pspWareProperties.url()),
                () -> assertEquals("psp-ware-token", pspWareProperties.token())
        );
    }
}