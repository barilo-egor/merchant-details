package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements MerchantDetailsResponse {

    private Payment payment;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.nonNull(payment)) {
            if (Objects.isNull(payment.getId())) {
                result.notNull("payment.id");
            }
            if (Objects.isNull(payment.getCredential())) {
                result.notNull("payment.credential");
            }
            if (Objects.isNull(payment.getCredentialAdditionalBank())) {
                result.notNull("payment.credentialAdditionalBank");
            }
            if (Objects.isNull(payment.getStatus())) {
                result.notNull("payment.status");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(payment);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payment {
        private Long id;

        private String credential;

        @JsonProperty("credential_additional_bank")
        private String credentialAdditionalBank;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
