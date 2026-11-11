package tgb.cryptoexchange.merchantdetails.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DdlAutoValidationEnvironmentPostProcessorTest {

    private final DdlAutoValidationEnvironmentPostProcessor postProcessor = new DdlAutoValidationEnvironmentPostProcessor();

    @ParameterizedTest
    @CsvSource(value = {
            "dev, update",
            "dev, create-drop",
            "dev, NULL",
            "'dev,kafka-disabled', update",
            "NULL, validate",
            "kafka-disabled, validate",
            "prod, VALIDATE"
    }, nullValues = "NULL")
    void shouldAllowStartup(String profiles, String ddlAuto) {
        MockEnvironment environment = environment(profiles, ddlAuto);

        assertDoesNotThrow(() -> postProcessor.postProcessEnvironment(environment, new SpringApplication()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "NULL, update",
            "NULL, NULL",
            "prod, update",
            "prod, none",
            "kafka-disabled, create-drop",
            "development, update"
    }, nullValues = "NULL")
    void shouldFailStartupWithoutDevProfileIfDdlAutoIsNotValidate(String profiles, String ddlAuto) {
        MockEnvironment environment = environment(profiles, ddlAuto);

        assertThrows(IllegalStateException.class,
                () -> postProcessor.postProcessEnvironment(environment, new SpringApplication()));
    }

    private static MockEnvironment environment(String profiles, String ddlAuto) {
        MockEnvironment environment = new MockEnvironment();
        if (profiles != null) {
            environment.setActiveProfiles(profiles.split(","));
        }
        if (ddlAuto != null) {
            environment.setProperty(DdlAutoValidationEnvironmentPostProcessor.DDL_AUTO_PROPERTY, ddlAuto);
        }
        return environment;
    }
}
