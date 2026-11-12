package tgb.cryptoexchange.merchantdetails.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.util.CollectionUtils;
import tgb.cryptoexchange.merchantdetails.config.JsonMappers;
import tgb.cryptoexchange.merchantdetails.details.BotDetailsRequest;
import tgb.cryptoexchange.merchantdetails.exception.DeserializeEventException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.EnumFeature;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class BotDetailsRequestDeserializer implements Deserializer<BotDetailsRequest> {

    private final ObjectMapper objectMapper = JsonMappers.builder()
            .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .build();

    @Override
    public BotDetailsRequest deserialize(String topic, byte[] data) {
        try {
            if (data == null) return null;
            BotDetailsRequest request = objectMapper.readValue(data, BotDetailsRequest.class);
            if (Objects.nonNull(request) && !CollectionUtils.isEmpty(request.getMethods())) {
                var partitioned = request.getMethods().stream()
                        .collect(Collectors.partitioningBy(method -> Objects.nonNull(method.getMerchant())));

                List<BotDetailsRequest.MerchantMethod> validMethods = partitioned.get(true);
                List<BotDetailsRequest.MerchantMethod> invalidMethods = partitioned.get(false);

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
