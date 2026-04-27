package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.BodyMappingException;

import java.util.Objects;

@Data
public class DetailsResponse {

    private String requestId;

    private Merchant merchant;

    @JsonIgnore
    private String details;

    @JsonIgnore
    private String bank;

    @JsonIgnore
    private String operator;

    private String merchantOrderId;

    private String merchantOrderStatus;

    private String merchantCustomId;

    private Integer amount;

    private String paymentMethod;

    private String qr;

    @JsonProperty("details")
    public String getFullDetails() {
        String result;
        if (Objects.nonNull(this.bank)) {
            result = this.bank;
        } else result = this.operator;
        result += " ";
        result += this.details;
        return result;
    }

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
