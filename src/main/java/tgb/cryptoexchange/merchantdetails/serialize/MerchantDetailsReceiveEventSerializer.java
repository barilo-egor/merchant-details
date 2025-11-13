package tgb.cryptoexchange.merchantdetails.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.merchantdetails.dto.MerchantDetailsReceiveEvent;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;

@Slf4j
public class MerchantDetailsReceiveEventSerializer implements Serializer<MerchantDetailsReceiveEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, MerchantDetailsReceiveEvent merchantDetailsReceiveEvent) {
        try {
            if (merchantDetailsReceiveEvent == null) {
                return null;
            }
            return objectMapper.writeValueAsBytes(merchantDetailsReceiveEvent);
        } catch (Exception e) {
            log.error("Ошибка сериализации объекта для отправки в топик {}: {}", topic, merchantDetailsReceiveEvent);
            throw new BodyMappingException("Error occurred while mapping merchantHistory", e);
        }
    }
}
