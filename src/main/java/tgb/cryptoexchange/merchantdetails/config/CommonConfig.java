package tgb.cryptoexchange.merchantdetails.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tgb.cryptoexchange.merchantdetails.kafka.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
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
}
