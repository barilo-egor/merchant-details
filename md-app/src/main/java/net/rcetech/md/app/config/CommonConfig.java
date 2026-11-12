package net.rcetech.md.app.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.config.MdcTaskDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableAsync
public class CommonConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    @Profile("!kafka-disabled")
    public JsonMapper objectMapper() {
        return JsonMappers.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

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
