package tgb.cryptoexchange.merchantdetails.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Бины, общие для интеграций с площадками: настройки площадок, URL callback'а и пул асинхронных запросов.
 */
@Configuration
@ConfigurationPropertiesScan("tgb.cryptoexchange.merchantdetails.properties")
public class IntegrationsConfig {

    @Bean
    public CallbackConfig callbackConfig(@Value("${callback-secret}") String callbackSecret, @Value("${gateway-url}") String gatewayUrl) {
        CallbackConfig callbackConfig = new CallbackConfig();
        callbackConfig.setCallbackSecret(callbackSecret);
        callbackConfig.setGatewayUrl(gatewayUrl);
        return callbackConfig;
    }

    @Bean(name = "asyncRequestExecutor")
    public ThreadPoolTaskExecutor asyncRequestExecutor(
            @Value("${async-request.executor.core-pool-size:2}") Integer corePoolSize,
            @Value("${async-request.executor.max-pool-size:8}") Integer maxPoolSize,
            @Value("${async-request.executor.queue-capacity:500}") Integer queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("AsyncRequest-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
