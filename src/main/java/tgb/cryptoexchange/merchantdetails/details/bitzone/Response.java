package tgb.cryptoexchange.merchantdetails.details.bitzone;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Requisite requisite;

    @JsonDeserialize(using = Method.Deserializer.class)
    private Method method;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(method)) {
            result.notNull("method");
        }
        if (Objects.nonNull(requisite)) {
            if (Objects.isNull(requisite.getBank())) {
                result.notNull("requisite.bank");
            }
            if (Objects.isNull(requisite.getRequisites()) && Objects.isNull(requisite.getSbpNumber())) {
                result.notNull("requisite.requisites", "requisite.sbpNumber");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }

    @Data
    public static class Requisite {

        private String bank;

        private String sbpNumber;

        private String requisites;
    }
}
