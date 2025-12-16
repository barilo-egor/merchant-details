package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.merchantdetails.dto.DetailsReceiveMonitorDTO;

@Component
@Slf4j
@Profile({"!kafka-disabled"})
public class DetailsReceiveMonitorProducerListener implements ProducerListener<String, DetailsReceiveMonitorDTO> {

    @Override
    public void onSuccess(ProducerRecord<String, DetailsReceiveMonitorDTO> producerRecord, RecordMetadata recordMetadata) {
        log.debug("Успешно отправлен DetailsReceiveMonitorDTO. Key={}, callback={}.", producerRecord.key(), producerRecord.value());
    }

    @Override
    public void onError(ProducerRecord<String, DetailsReceiveMonitorDTO> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.error("Ошибка при попытке отправить DetailsReceiveMonitorDTO в топик. Key={}, callback={}: {}.",
                producerRecord.key(), producerRecord.value(), exception.getMessage(), exception);
    }
}