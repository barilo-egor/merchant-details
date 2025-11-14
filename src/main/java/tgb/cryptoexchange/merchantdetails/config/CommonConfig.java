package tgb.cryptoexchange.merchantdetails.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEvent;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CommonConfig {

    private final KafkaProperties kafkaProperties;

    public CommonConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Bean
    public ProducerFactory<String, MerchantDetailsReceiveEvent> merchantHistoryProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MerchantDetailsReceiveEventSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, MerchantDetailsReceiveEvent> kafkaTemplate() {
        return new KafkaTemplate<>(merchantHistoryProducerFactory());
    }
}
