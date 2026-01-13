package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response implements MerchantDetailsResponse {

    private String amount;

    private Integer currencyRate;

    private String referenceId;

    private String id;

    private Requisite requisite;

    private Status status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @Data
    public static class Requisite {

        private String currency;

        private RequisiteType requisiteType;

        private String bankCode;

        private List<GeoType> geocodes;

        private String requisite;

        private String recipient;

        @JsonProperty("bik")
        private String bancCode;

        private String bankName;

        private String paymentUrl;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(amount)) {
            result.notNull("amount");
        }
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(requisite)) {
            result.notNull("requisite");
        }
        if (Objects.isNull(requisite.getCurrency())) {
            result.notNull("requisite.currency");
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }
}
