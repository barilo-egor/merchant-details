package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private String customId;

    private Method paymentMethod;

    private Integer fiatSum;

    @JsonProperty("fiatCurrencyCode")
    public String getFiatCurrencyCode() {
        return "RUB";
    }

    @JsonProperty("cryptoCurrencyCode")
    public String getCryptoCurrencyCode() {
        return "USDT";
    }
}
