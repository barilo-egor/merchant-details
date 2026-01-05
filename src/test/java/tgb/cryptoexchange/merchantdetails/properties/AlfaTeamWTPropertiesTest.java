package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AlfaTeamWTPropertiesTest {

    @Autowired
    private AlfaTeamWTProperties alfaTeamWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("alfa-team-white-triangle-url", alfaTeamWTProperties.url()),
                () -> assertEquals("alfa-team-white-triangle-key", alfaTeamWTProperties.key()),
                () -> assertEquals("alfa-team-white-triangle-token", alfaTeamWTProperties.token()),
                () -> assertEquals("alfa-team-white-triangle-secret", alfaTeamWTProperties.secret())
        );
    }
}