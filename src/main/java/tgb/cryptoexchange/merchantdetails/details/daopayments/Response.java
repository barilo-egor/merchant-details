package tgb.cryptoexchange.merchantdetails.details.daopayments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("transfer_details")
    private TransferDetails transferDetails;

    private String amount;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(transactionId)) {
            result.notNull("transactionId");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.nonNull(transferDetails)) {
            if (Objects.isNull(transferDetails.getBankName())) {
                result.notNull("transferDetails.bankName");
            }
            if (Objects.isNull(transferDetails.getCardNumber())) {
                result.notNull("transferDetails.cardNumber");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(transferDetails) && Objects.nonNull(transferDetails.getCardNumber())
                && Objects.nonNull(transferDetails.getBankName());
    }

    @Data
    public static class TransferDetails {

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("bank_name")
        private String bankName;
    }
}
