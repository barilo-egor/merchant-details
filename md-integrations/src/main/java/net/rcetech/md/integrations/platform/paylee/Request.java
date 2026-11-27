package net.rcetech.md.integrations.platform.paylee;

import lombok.Data;

@Data
public class Request {

    private Integer price;

    private Method requisitesType;

    private String clientId;

    private String amountCurrency;

    public String getAmountCurrency() {
        return "USDT";
    }
}
