package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.util.CollectionUtils;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class BotDetailsRequest {

    protected String requestId;

    protected String id;

    @NotNull
    @Min(1)
    protected Integer amount;

    protected String userId;

    protected List<MerchantMethod> methods;

    protected String initiatorApp;

    @JsonIgnore
    public List<String> getMerchantMethod(Merchant merchant) {
        for (MerchantMethod merchantMethod : methods) {
            if (merchantMethod.getMerchant().equals(merchant)) {
                return merchantMethod.getMethods();
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

        private List<String> methods;

    }

    public static class KafkaDeserializer implements Deserializer<BotDetailsRequest> {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        @Override
        public BotDetailsRequest deserialize(String topic, byte[] data) {
            try {
                if (data == null) return null;
                BotDetailsRequest request = objectMapper.readValue(data, BotDetailsRequest.class);
                if (Objects.nonNull(request) && !CollectionUtils.isEmpty(request.getMethods())) {
                    var partitioned = request.getMethods().stream()
                            .collect(Collectors.partitioningBy(method -> Objects.nonNull(method.getMerchant())));

                    List<MerchantMethod> validMethods = partitioned.get(true);
                    List<MerchantMethod> invalidMethods = partitioned.get(false);

                    if (!invalidMethods.isEmpty()) {
                        invalidMethods.forEach(invalidMethod -> log.warn(
                                "Найден не существующий Merchant enum в топике '{}'",
                                topic
                        ));
                    }
                    request.setMethods(validMethods);
                }
                return request;
            } catch (Exception e) {
                throw new DeserializeEventException("Error occurred while deserializer value: " + new String(data, StandardCharsets.UTF_8), e);
            }
        }
    }
}
