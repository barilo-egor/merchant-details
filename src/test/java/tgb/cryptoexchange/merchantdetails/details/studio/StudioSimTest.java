package tgb.cryptoexchange.merchantdetails.details.studio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import tgb.cryptoexchange.merchantdetails.properties.StudioSimProperties;

import static org.assertj.core.api.Assertions.assertThat;

class StudioSimTest {

    @Configuration
    @EnableConfigurationProperties(StudioSimProperties.class)
    static class TestConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.context.annotation.UserConfigurations.of(TestConfig.class))
            .withPropertyValues(
                    "studio-sim.url=https://sim-api.com",
                    "studio-sim.key-sim=secret-sim-key"
            );

    @Test
    void testStudioSimPath() {
        contextRunner.run(context -> {
            String expectedName = "studio-sim-tgb.cryptoexchange.merchantdetails.properties.StudioSimProperties";
            if (context.getStartupFailure() != null) {
                context.getStartupFailure().printStackTrace();
            }
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean(expectedName);
        });
    }

}
