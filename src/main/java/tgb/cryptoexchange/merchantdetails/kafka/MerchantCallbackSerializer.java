package tgb.cryptoexchange.merchantdetails.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;

@Slf4j
public class MerchantCallbackSerializer implements Serializer<MerchantCallbackEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, MerchantCallbackEvent callback) {
        try {
            if (callback == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(callback);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации callback для отправки в топик {}: {}", topic, callback);
            throw new BodyMappingException("Error occurred while mapping merchant callback", e);
        }
    }
}
