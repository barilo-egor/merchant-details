package net.rcetech.md.integrations.platform.bridgepay;

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

    private Method paymentOption;

    private Boolean startDeal;

    private String crossBorderCurrency;
}
