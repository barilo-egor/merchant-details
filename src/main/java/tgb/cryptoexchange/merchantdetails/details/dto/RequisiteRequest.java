package tgb.cryptoexchange.merchantdetails.details.dto;

import lombok.Data;

@Data
public class RequisiteRequest {

    private String method;

    private Integer amount;

    private String callbackUrl;
}
