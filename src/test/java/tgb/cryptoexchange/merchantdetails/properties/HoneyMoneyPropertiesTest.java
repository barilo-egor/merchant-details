package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HoneyMoneyPropertiesTest {

    @Autowired
    private HoneyMoneyProperties honeyMoneyProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("honey-money-urls-token", honeyMoneyProperties.urls().token()),
                () -> assertEquals("honey-money-url-main", honeyMoneyProperties.urls().main()),
                () -> assertEquals("honey-money-sign-key", honeyMoneyProperties.signKey()),
                () -> assertEquals("honey-money-client-id", honeyMoneyProperties.clientId()),
                () -> assertEquals("honey-money-secret", honeyMoneyProperties.secret())
        );
    }
}