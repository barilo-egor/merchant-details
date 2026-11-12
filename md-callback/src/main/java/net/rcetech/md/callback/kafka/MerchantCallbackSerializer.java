package net.rcetech.md.callback.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.meta.exception.BodyMappingException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class MerchantCallbackSerializer implements Serializer<MerchantCallbackEvent> {

    private static final ObjectMapper objectMapper = JsonMappers.create();

    @Override
    public byte[] serialize(String topic, MerchantCallbackEvent callback) {
        try {
            if (callback == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(callback);
        } catch (JacksonException e) {
            log.error("Ошибка сериализации callback для отправки в топик {}: {}", topic, callback);
            throw new BodyMappingException("Error occurred while mapping merchant callback", e);
        }
    }
}
