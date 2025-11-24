package tgb.cryptoexchange.merchantdetails.details;

import lombok.Data;

@Data
public class CancelOrderRequest {

    private String orderId;

    private String method;
}
