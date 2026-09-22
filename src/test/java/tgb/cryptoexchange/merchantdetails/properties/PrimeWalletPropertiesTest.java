package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PrimeWalletPropertiesTest {

    @Autowired
    private PrimeWalletProperties primeWalletProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("prime-wallet-url", primeWalletProperties.url()),
                () -> assertEquals("prime-wallet-token", primeWalletProperties.token())
        );
    }
}