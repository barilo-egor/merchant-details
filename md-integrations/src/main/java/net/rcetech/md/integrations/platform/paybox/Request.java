package net.rcetech.md.integrations.platform.paybox;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("merchant_transaction_id")
    private String merchantTransactionId;

    private Integer amount;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("payer_bank_name")
    private String payerBankName;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
