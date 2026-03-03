package tgb.cryptoexchange.merchantdetails.details.neuralpay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Currency;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    private Integer amount;

    @JsonProperty("paymentMethods")
    private List<String> method;

    private Requisite requisite = new Requisite();

    @Data
    public static class Requisite {

        private final String currency = Currency.getInstance("RUB").getCurrencyCode();

    }

}
