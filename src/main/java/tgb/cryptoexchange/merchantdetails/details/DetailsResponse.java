package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;

@Data
public class DetailsResponse {

    private String requestId;

    private Merchant merchant;

    private String details;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private String merchantCustomId;

    private Integer amount;

    private String qr;

    @Slf4j
    public static class KafkaSerializer implements Serializer<DetailsResponse> {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(String topic, DetailsResponse detailsResponse) {
            try {
                if (detailsResponse == null) {
                    return new byte[0];
                }
                return objectMapper.writeValueAsBytes(detailsResponse);
            } catch (JsonProcessingException e) {
                log.error("Ошибка сериализации объекта для отправки в топик {}: {}", topic, detailsResponse);
                throw new BodyMappingException("Error occurred while mapping detailsResponse", e);
            }
        }
    }
}
