package tgb.cryptoexchange.merchantdetails.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Запрещает запуск без профиля {@code dev}, если {@code spring.jpa.hibernate.ddl-auto} не равен {@code validate}.
 * Выполняется до создания контекста, поэтому Hibernate не успевает изменить схему.
 */
public class DdlAutoValidationEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String DDL_AUTO_PROPERTY = "spring.jpa.hibernate.ddl-auto";

    static final String REQUIRED_DDL_AUTO = "validate";

    static final String DEV_PROFILE = "dev";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.matchesProfiles(DEV_PROFILE)) {
            return;
        }
        String ddlAuto = environment.getProperty(DDL_AUTO_PROPERTY);
        if (!REQUIRED_DDL_AUTO.equalsIgnoreCase(ddlAuto)) {
            throw new IllegalStateException(
                    "Без профиля %s свойство %s должно быть равно %s, текущее значение: %s"
                            .formatted(DEV_PROFILE, DDL_AUTO_PROPERTY, REQUIRED_DDL_AUTO, ddlAuto));
        }
    }
}
