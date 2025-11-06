package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.Data;

@Data
public class Request {

    private String extId;

    private Integer amount;

    private String callbackUrl;

    private String bank;

    public String getCurrency() {
        return "RUB";
    }
}
