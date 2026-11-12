package tgb.cryptoexchange.merchantdetails;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Базовый класс для тестов модуля, которым нужен контекст Spring: настройки площадок и бины интеграций.
 */
@SpringBootTest(classes = IntegrationsTestApplication.class)
public abstract class AbstractIntegrationsContextTest {
}
