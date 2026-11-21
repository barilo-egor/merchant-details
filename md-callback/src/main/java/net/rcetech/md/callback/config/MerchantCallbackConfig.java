package net.rcetech.md.callback.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.platform.CallbackDecryptService;
import net.rcetech.md.callback.kafka.MerchantCallbackEvent;
import net.rcetech.md.callback.kafka.MerchantCallbackProducerListener;
import net.rcetech.md.callback.kafka.MerchantCallbackSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Бины приёма callback'ов площадок: расшифровщики тел и отправка статусов ордеров в Kafka.
 */
@Configuration
public class MerchantCallbackConfig {

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
    public Map<Merchant, CallbackDecryptService> callbackDecryptServiceMap(List<CallbackDecryptService> services) {
        return services.stream()
                .collect(Collectors.toMap(
                        CallbackDecryptService::getMerchant,
                        service -> service
                ));
    }
}
