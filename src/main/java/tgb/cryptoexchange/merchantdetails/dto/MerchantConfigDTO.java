package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.MerchantConstants;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.MerchantSuccessStatus;
import tgb.cryptoexchange.merchantdetails.enums.RequiredReceipt;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantConfigDTO {

    private Long id;

    private Boolean isOn;

    private Merchant merchant;

    private Boolean isAutoWithdrawalOn;

    @JsonSerialize(contentUsing = MerchantOrderStatusSerializer.class)
    private List<MerchantOrderStatus> statuses;

    private List<String> successStatuses;

    @JsonSerialize(contentUsing = MerchantMethodSerializer.class)
    private List<MerchantMethod> methods;

    private Integer maxAmount;

    private Integer minAmount;

    private Integer merchantOrder;

    private List<AutoConfirmConfigDTO> confirmConfigs;

    private Long groupChatId;

    private Integer minDealsCount;

    private RequiredReceipt requiredReceipt;

    public static MerchantConfigDTO fromEntity(MerchantConfig merchantConfig) {
        MerchantConfigDTO merchantConfigDTO = new MerchantConfigDTO();
        merchantConfigDTO.setId(merchantConfig.getId());
        merchantConfigDTO.setIsOn(merchantConfig.getIsOn());
        merchantConfigDTO.setMerchant(merchantConfig.getMerchant());
        merchantConfigDTO.setIsAutoWithdrawalOn(merchantConfig.getIsAutoWithdrawalOn());
        merchantConfigDTO.setStatuses(MerchantConstants.getStatuses(merchantConfig.getMerchant()));
        if (Objects.nonNull(merchantConfig.getSuccessStatuses())) {
            merchantConfigDTO.setSuccessStatuses(merchantConfig.getSuccessStatuses().stream()
                    .map(MerchantSuccessStatus::getStatus)
                    .toList());
        }
        merchantConfigDTO.setMethods(MerchantConstants.getMethods(merchantConfig.getMerchant()));
        merchantConfigDTO.setMaxAmount(merchantConfig.getMaxAmount());
        merchantConfigDTO.setMinAmount(merchantConfig.getMinAmount());
        merchantConfigDTO.setMerchantOrder(merchantConfig.getMerchantOrder());
        if (Objects.nonNull(merchantConfig.getConfirmConfigs())) {
            merchantConfigDTO.setConfirmConfigs(
                    merchantConfig.getConfirmConfigs().stream().map(AutoConfirmConfigDTO::fromEntity).toList()
            );
        }
        merchantConfigDTO.setGroupChatId(merchantConfig.getGroupChatId());
        merchantConfigDTO.setMinDealsCount(merchantConfig.getMinDealsCount());
        merchantConfigDTO.setRequiredReceipt(merchantConfig.getRequiredReceipt());
        return merchantConfigDTO;
    }

    public static class MerchantMethodSerializer extends ValueSerializer<MerchantMethod> {

        @Override
        public void serialize(MerchantMethod merchantMethod, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("name", merchantMethod.name());
            jsonGenerator.writeStringProperty("description", merchantMethod.getDescription());
            jsonGenerator.writeEndObject();
        }
    }

    public static class MerchantOrderStatusSerializer extends ValueSerializer<MerchantOrderStatus> {
        @Override
        public void serialize(MerchantOrderStatus merchantOrderStatus, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("name", merchantOrderStatus.name());
            jsonGenerator.writeStringProperty("description", merchantOrderStatus.getDescription());
            jsonGenerator.writeEndObject();
        }
    }
}
