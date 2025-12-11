package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile({"!kafka-disabled"})
public class MerchantCallbackProducerListener implements ProducerListener<String, MerchantCallbackEvent> {

    @Override
    public void onSuccess(ProducerRecord<String, MerchantCallbackEvent> producerRecord, RecordMetadata recordMetadata) {
        log.debug("Успешно отправлен callback. Key={}, callback={}.", producerRecord.key(), producerRecord.value());
    }

    @Override
    public void onError(ProducerRecord<String, MerchantCallbackEvent> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.error("Ошибка при попытке отправить callback в топик. Key={}, callback={}: {}.",
                producerRecord.key(), producerRecord.value(), exception.getMessage(), exception);
    }
}