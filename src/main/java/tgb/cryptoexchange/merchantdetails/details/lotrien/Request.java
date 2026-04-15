package tgb.cryptoexchange.merchantdetails.details.lotrien;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("paymentMethods")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private String fiatSum;

    @JsonProperty("fiatCurrencyCode")
    public String getFiatCurrencyCode() {
        return "RUB";
    }

    @JsonProperty("cryptoCurrencyCode")
    public String getCryptoCurrencyCode() {
        return "USDT";
    }
}
