package net.rcetech.md.app.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import net.rcetech.md.integrations.config.JsonMappers;
import net.rcetech.md.integrations.platform.DetailsResponse;
import net.rcetech.md.meta.exception.BodyMappingException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class DetailsResponseSerializer implements Serializer<DetailsResponse> {

    private static final ObjectMapper objectMapper = JsonMappers.create();

    @Override
    public byte[] serialize(String topic, DetailsResponse detailsResponse) {
        try {
            if (detailsResponse == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(detailsResponse);
        } catch (JacksonException e) {
            log.error("Ошибка сериализации объекта для отправки в топик {}: {}", topic, detailsResponse);
            throw new BodyMappingException("Error occurred while mapping detailsResponse", e);
        }
    }
}
