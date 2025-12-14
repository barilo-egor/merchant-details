package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequest {

    private String requestId;

    private Long id;

    private String method;

    @NotNull
    @Min(1)
    private Integer amount;

    private Long chatId;

    private String initiatorApp;

    @NotEmpty
    private List<MerchantMethod> methods;

    public Optional<String> getMethod(Merchant merchant) {
        if (Objects.isNull(methods)) {
            return Optional.of(method);
        }
        for (MerchantMethod merchantMethod : methods) {
            if (merchantMethod.getMerchant().equals(merchant)) {
                return Optional.of(merchantMethod.getMethod());
            }
        }
        return Optional.empty();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchantMethod {

        private Merchant merchant;

        private String method;
    }

    public static class KafkaDeserializer implements Deserializer<DetailsRequest> {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        @Override
        public DetailsRequest deserialize(String topic, byte[] data) {
            try {
                if (data == null) return null;
                return objectMapper.readValue(data, DetailsRequest.class);
            } catch (Exception e) {
                throw new DeserializeEventException("Error occurred while deserializer value: " + new String(data, StandardCharsets.UTF_8), e);
            }
        }
    }
}
