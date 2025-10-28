package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {
    private String type = "in";

    private String amount;

    private String currency;

    private String notificationUrl;

    private String notificationToken;

    private String internalId;

    private String userId;

    @JsonSerialize(using = PaymentOption.Serializer.class)
    private PaymentOption paymentOption;

    private Boolean startDeal;

    private String crossBorderCurrency;
}
