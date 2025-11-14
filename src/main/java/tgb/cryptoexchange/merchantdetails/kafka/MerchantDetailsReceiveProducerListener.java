package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantDetailsReceiveProducerListener implements ProducerListener<String, MerchantDetailsReceiveEvent> {

    @Override
    public void onSuccess(ProducerRecord<String, MerchantDetailsReceiveEvent> producerRecord, RecordMetadata recordMetadata) {
        log.debug("Успешно отправлен ивент. Key={}, event={}.", producerRecord.key(), producerRecord.value());
    }

    @Override
    public void onError(ProducerRecord<String, MerchantDetailsReceiveEvent> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.error("Ошибка при попытке отправить ивент в топик. Key={}, event={}: {}.",
                producerRecord.key(), producerRecord.value(), exception.getMessage(), exception);
    }
}
