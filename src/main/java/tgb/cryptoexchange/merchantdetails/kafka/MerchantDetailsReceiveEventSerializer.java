package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.merchantdetails.config.JsonMappers;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class MerchantDetailsReceiveEventSerializer implements Serializer<MerchantDetailsReceiveEvent> {

    private static final ObjectMapper objectMapper = JsonMappers.create();

    @Override
    public byte[] serialize(String topic, MerchantDetailsReceiveEvent merchantDetailsReceiveEvent) {
        try {
            if (merchantDetailsReceiveEvent == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(merchantDetailsReceiveEvent);
        } catch (JacksonException e) {
            log.error("Ошибка сериализации объекта для отправки в топик {}: {}", topic, merchantDetailsReceiveEvent);
            throw new BodyMappingException("Error occurred while mapping merchantHistory", e);
        }
    }
}
