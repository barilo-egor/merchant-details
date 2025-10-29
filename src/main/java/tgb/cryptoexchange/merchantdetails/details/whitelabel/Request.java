package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {
    private String type = "in";

    private String amount;

    private String currency;

    private String notificationUrl;

    private String notificationToken;

    private String internalId;

    private String userId;

    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentOption;

    private Boolean startDeal;

    private String crossBorderCurrency;
}
