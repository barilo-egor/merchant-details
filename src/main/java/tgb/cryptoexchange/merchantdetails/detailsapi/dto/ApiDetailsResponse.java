package tgb.cryptoexchange.merchantdetails.detailsapi.dto;

import lombok.Data;

@Data
public class ApiDetailsResponse {

    private String requestId;

    private String merchant;

    private String orderId;

    private String orderStatus;

    private Details details;

    private Integer amount;

}
