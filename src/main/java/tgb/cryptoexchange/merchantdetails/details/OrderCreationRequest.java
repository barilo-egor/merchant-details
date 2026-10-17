package tgb.cryptoexchange.merchantdetails.details;

import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationRequest {

    protected String requestId;

    protected String id;

    protected Integer amount;

    protected String userId;

    protected String method;

    protected String redirectUrl;
    protected String phone;

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

    public static class KafkaDeserializer implements Deserializer<OrderCreationRequest> {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        @Override
        public OrderCreationRequest deserialize(String topic, byte[] data) {
            try {
                if (data == null) return null;
                OrderCreationRequest request = objectMapper.readValue(data, OrderCreationRequest.class);
                if (Objects.nonNull(request) && !CollectionUtils.isEmpty(request.getMethods())) {
                    var partitioned = request.getMethods().stream()
                            .collect(Collectors.partitioningBy(method -> Objects.nonNull(method.getMerchant())));

}
