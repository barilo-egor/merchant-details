package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequest {

    protected String requestId;

    protected Long id;

    @NotNull
    @Min(1)
    protected Integer amount;

    protected Long chatId;

    protected String initiatorApp;

    protected List<MerchantMethod> methods;

    public String getCurrentMerchantMethod() {
        throw new UnsupportedOperationException("CurrentMerchantMethod is not implemented yet");
    }

    @JsonIgnore
    public List<String> getMerchantMethod(Merchant merchant) {
        for (MerchantMethod merchantMethod : methods) {
            if (merchantMethod.getMerchant().equals(merchant)) {
                return merchantMethod.getMethod();
            }
        }
        return Collections.emptyList();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchantMethod {

        private Merchant merchant;

        private List<String> method;

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
