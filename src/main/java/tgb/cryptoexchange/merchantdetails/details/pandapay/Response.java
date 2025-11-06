package tgb.cryptoexchange.merchantdetails.details.pandapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String uuid;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("requisite_data")
    private RequisiteData requisiteData;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(status)) {
            result.notNull("status");
        } else if (!Status.TRADER_NOT_FOUND.equals(status)) {
            if (Objects.isNull(uuid)) {
                result.notNull("uuid");
            }
            if (Objects.isNull(requisiteData)) {
                result.notNull("requisiteData");
            } else {
                if (Objects.isNull(requisiteData.getRequisites())) {
                    result.notNull("requisiteData.requisites");
                }
                if (Objects.isNull(requisiteData.getBank())) {
                    result.notNull("requisiteData.bank");
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return !Status.TRADER_NOT_FOUND.equals(status);
    }

    @Data
    public static class RequisiteData {

        @JsonProperty("bank_name_ru")
        private String bank;

        private String requisites;
    }
}
