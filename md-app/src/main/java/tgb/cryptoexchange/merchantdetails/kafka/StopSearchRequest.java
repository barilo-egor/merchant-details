package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.Data;
import org.apache.kafka.common.serialization.Deserializer;
import tgb.cryptoexchange.merchantdetails.config.JsonMappers;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Data
public class StopSearchRequest {

    /**
     * Идентификатор сделки
     */
    private Long id;

    public static class KafkaDeserializer implements Deserializer<StopSearchRequest> {

        private final ObjectMapper objectMapper = JsonMappers.create();

        @Override
        public StopSearchRequest deserialize(String topic, byte[] data) {
            try {
                if (data == null) return null;
                return objectMapper.readValue(data, StopSearchRequest.class);
            } catch (Exception e) {
                throw new DeserializeEventException("Error occurred while deserializer value: " + new String(data, StandardCharsets.UTF_8), e);
            }
        }
    }
}
