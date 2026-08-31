package tgb.cryptoexchange.merchantdetails.details.wat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private boolean success;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(data.getId())) {
                result.notNull("data.id");
            }
            if (Objects.isNull(data.getRequisite())) {
                result.notNull("data.requisite");
            }
            if (Objects.isNull(data.getStatus())) {
                result.notNull("data.status");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data.getBank()) && Objects.nonNull(data.getBank().getName());
    }

    @lombok.Data
    public static class Data {

        private String id;

        private String requisite;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        @JsonProperty("bank_info")
        private BankInfo bank;

        @lombok.Data
        public static class BankInfo {

            private String name;

        }
    }
}
