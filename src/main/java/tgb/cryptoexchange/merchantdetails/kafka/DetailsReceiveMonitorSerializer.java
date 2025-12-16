package tgb.cryptoexchange.merchantdetails.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.merchantdetails.dto.DetailsReceiveMonitorDTO;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;

@Slf4j
public class DetailsReceiveMonitorSerializer implements Serializer<DetailsReceiveMonitorDTO> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, DetailsReceiveMonitorDTO callback) {
        try {
            if (callback == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(callback);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации DetailsReceiveMonitorDTO для отправки в топик {}: {}", topic, callback);
            throw new BodyMappingException("Error occurred while mapping DetailsReceiveMonitorDTO", e);
        }
    }
}
