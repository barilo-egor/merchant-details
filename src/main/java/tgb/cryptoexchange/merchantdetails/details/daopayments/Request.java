package tgb.cryptoexchange.merchantdetails.details.daopayments;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String amount;

    private final String currency = "RUB";

    @JsonProperty("merchant_order_id")
    private String merchantOrderId;

    @JsonProperty("success_url")
    private String successUrl;

    @JsonProperty("fail_url")
    private String failUrl;

    @JsonProperty("requisite_type")
    @JsonSerialize(using = Method.Serializer.class)
    private Method requisiteType;
}
