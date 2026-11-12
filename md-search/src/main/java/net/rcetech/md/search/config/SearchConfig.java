package net.rcetech.md.search.config;

import net.rcetech.md.integrations.config.MdcTaskDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Бины поиска реквизитов для процессинга.
 */
@Configuration
public class SearchConfig {

    @Bean(name = "detailsRequestSearchExecutorApi")
    public ThreadPoolTaskExecutor detailsRequestSearchExecutorApi(
            @Value("${details.executor.core-pool-size}") Integer corePoolSize,
            @Value("${details.executor.max-pool-size}") Integer maxPoolSize,
            @Value("${details.executor.queue-capacity}") Integer queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("DetailsRequestSearchApi-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
