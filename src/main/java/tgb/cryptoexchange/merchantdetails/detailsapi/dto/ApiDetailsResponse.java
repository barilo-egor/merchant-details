package tgb.cryptoexchange.merchantdetails.detailsapi.dto;

import lombok.Data;

@Data
public class ApiDetailsResponse {

    private String requestId;

    private String orderId;

    private Details details;

    private Integer amount;

}
