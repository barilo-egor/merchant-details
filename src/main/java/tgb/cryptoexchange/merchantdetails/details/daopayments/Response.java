package tgb.cryptoexchange.merchantdetails.details.daopayments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("transfer_details")
    private TransferDetails transferDetails;

    private String amount;

    @Data
    public static class TransferDetails {

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("bank_name")
        private String bankName;
    }
}
