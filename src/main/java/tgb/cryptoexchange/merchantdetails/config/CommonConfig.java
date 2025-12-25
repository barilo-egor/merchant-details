package tgb.cryptoexchange.merchantdetails.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.backoff.FixedBackOff;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.dto.DetailsReceiveMonitorDTO;
import tgb.cryptoexchange.merchantdetails.kafka.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Configuration
@EnableAsync
public class CommonConfig {

    @Bean
    @Profile("!kafka-disabled")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean
    @Profile("!kafka-disabled")
    public ProducerFactory<String, MerchantDetailsReceiveEvent> merchantHistoryProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MerchantDetailsReceiveEventSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Profile("!kafka-disabled")
    public KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate(MerchantDetailsReceiveProducerListener merchantDetailsReceiveProducerListener,
                                                                            KafkaProperties kafkaProperties) {
        KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate = new KafkaTemplate<>(merchantHistoryProducerFactory(kafkaProperties));
        kafkaTemplate.setProducerListener(merchantDetailsReceiveProducerListener);
        return kafkaTemplate;
    }

    @Bean
    @Profile("!kafka-disabled")
    public ProducerFactory<String, MerchantCallbackEvent> merchantCallbackProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MerchantCallbackSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Profile("!kafka-disabled")
    public KafkaTemplate<String, MerchantCallbackEvent> callbackKafkaTemplate(MerchantCallbackProducerListener merchantCallbackProducerListener,
                                                                              KafkaProperties kafkaProperties) {
        KafkaTemplate<String, MerchantCallbackEvent> kafkaTemplate = new KafkaTemplate<>(merchantCallbackProducerFactory(kafkaProperties));
        kafkaTemplate.setProducerListener(merchantCallbackProducerListener);
        return kafkaTemplate;
    }

    @Bean
    public CallbackConfig callbackConfig(@Value("${callback-secret}") String callbackSecret, @Value("${gateway-url}") String gatewayUrl) {
        CallbackConfig callbackConfig = new CallbackConfig();
        callbackConfig.setCallbackSecret(callbackSecret);
        callbackConfig.setGatewayUrl(gatewayUrl);
        return callbackConfig;
    }

    @Bean
    public ConsumerFactory<String, DetailsRequest> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, DetailsRequest.KafkaDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DetailsRequest> kafkaListenerContainerFactory(KafkaProperties kafkaProperties,
                                                                                                         DetailsRequestErrorService detailsRequestErrorService) {
        ConcurrentKafkaListenerContainerFactory<String, DetailsRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(kafkaProperties));
        factory.setCommonErrorHandler(defaultErrorHandler(detailsRequestErrorService));
        return factory;
    }


    @Bean
    public DefaultErrorHandler defaultErrorHandler(DetailsRequestErrorService detailsRequestErrorService) {
        return new DefaultErrorHandler(
                detailsRequestErrorService::handle,
                new FixedBackOff(60000, 1)
        );
    }

    @Bean
    @Profile("!kafka-disabled")
    public ProducerFactory<String, DetailsResponse> detailsResponseProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DetailsResponse.KafkaSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Profile("!kafka-disabled")
    public KafkaTemplate<String, DetailsResponse> detailsResponseKafkaTemplate(DetailsReponseFoundProducerListener detailsReponseFoundProducerListener,
                                                                               KafkaProperties kafkaProperties) {
        KafkaTemplate<String, DetailsResponse> kafkaTemplate = new KafkaTemplate<>(detailsResponseProducerFactory(kafkaProperties));
        kafkaTemplate.setProducerListener(detailsReponseFoundProducerListener);
        return kafkaTemplate;
    }

    @Bean(name = "detailsRequestSearchExecutor")
    public ThreadPoolTaskExecutor detailsRequestSearchExecutor(
            @Value("${details.executor.core-pool-size}") Integer corePoolSize,
            @Value("${details.executor.max-pool-size}") Integer maxPoolSize,
            @Value("${details.executor.queue-capacity}") Integer queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("DetailsRequestSearch-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Map<Long, Future<Void>> activeSearchMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    @Profile("!kafka-disabled")
    public ProducerFactory<String, DetailsReceiveMonitorDTO> detailsReceiveMonitorProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DetailsReceiveMonitorSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Profile("!kafka-disabled")
    public KafkaTemplate<String, DetailsReceiveMonitorDTO> detailsReceiveMonitorKafkaTemplate(DetailsReceiveMonitorProducerListener detailsReceiveMonitorProducerListener,
                                                                                              KafkaProperties kafkaProperties) {
        KafkaTemplate<String, DetailsReceiveMonitorDTO> kafkaTemplate = new KafkaTemplate<>(detailsReceiveMonitorProducerFactory(kafkaProperties));
        kafkaTemplate.setProducerListener(detailsReceiveMonitorProducerListener);
        return kafkaTemplate;
    }

    @Bean
    public ConsumerFactory<String, StopSearchRequest> stopSearchRequestConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StopSearchRequest.KafkaDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StopSearchRequest> stopSearchRequestContainerFactory(KafkaProperties kafkaProperties,
                                                                                                             DetailsRequestErrorService detailsRequestErrorService) {
        ConcurrentKafkaListenerContainerFactory<String, StopSearchRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stopSearchRequestConsumerFactory(kafkaProperties));
        factory.setCommonErrorHandler(defaultErrorHandler(detailsRequestErrorService));
        return factory;
    }
}
