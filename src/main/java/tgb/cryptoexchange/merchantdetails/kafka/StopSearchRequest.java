package tgb.cryptoexchange.merchantdetails.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.apache.kafka.common.serialization.Deserializer;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;

import java.nio.charset.StandardCharsets;

@Data
public class StopSearchRequest {

    /**
     * Идентификатор сделки
     */
    private Long id;

    public static class KafkaDeserializer implements Deserializer<StopSearchRequest> {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

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
