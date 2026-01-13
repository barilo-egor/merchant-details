package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RequisiteType {
    @JsonProperty("PAY-IN")
    PAY_IN,
    @JsonProperty("PAY-OUT")
    PAY_OUT
}