package net.rcetech.md.integrations.platform;

import lombok.Data;

@Data
public class CancelOrderRequest {

    private String orderId;

    private String method;
}
