package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DaoPaymentsPropertiesTest {

    @Autowired
    private DaoPaymentsProperties daoPaymentsProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("dao-payments-url", daoPaymentsProperties.url()),
                () -> assertEquals("dao-payments-key", daoPaymentsProperties.key())
        );
    }
}