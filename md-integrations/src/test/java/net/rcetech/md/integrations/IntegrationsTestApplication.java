package net.rcetech.md.integrations;

import net.rcetech.md.integrations.platform.smackpay.SmackPayClientIds;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * Конфигурация для тестов модуля: поднимает только интеграции с площадками, без БД, Kafka и контроллеров.
 */
@SpringBootApplication
public class IntegrationsTestApplication {

    /**
     * Реализация SmackPayClientIds живёт в md-app (таблица smack_pay_client_id),
     * в этом контексте БД нет — подставляем пустую заглушку.
     */
    @Bean
    SmackPayClientIds smackPayClientIds() {
        return userId -> Optional.empty();
    }
}
