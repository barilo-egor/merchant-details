package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
class Request {

    private String amountFiat;

    private FiatInfo fiatInfo;

    private String goodReturnLink;

    private String badReturnLink;

    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    @JsonProperty("type")
    public Integer getType() {
        return 0;
    }

    @JsonProperty("tokenCode")
    public String getTokenCode() {
        return "USDT";
    }

    @Data
    public static class FiatInfo {

        @JsonProperty("fiatCode")
        public String getFiatCode() {
            return "RUB";
        }
    }
}
