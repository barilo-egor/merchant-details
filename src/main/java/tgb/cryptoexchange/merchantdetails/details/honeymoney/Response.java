package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.Data;

@Data
public class Response {

    private Integer id;

    private String cardNumber;

    private String bankName;

    private String phoneNumber;
}
