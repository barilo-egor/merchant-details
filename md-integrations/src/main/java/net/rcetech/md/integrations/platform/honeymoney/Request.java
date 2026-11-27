package net.rcetech.md.integrations.platform.honeymoney;

import lombok.Data;

@Data
public class Request {

    private String extId;

    private Integer amount;

    private String callbackUrl;

    private String bank;

    private String currency;
}
