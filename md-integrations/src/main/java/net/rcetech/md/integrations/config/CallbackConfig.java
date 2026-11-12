package net.rcetech.md.integrations.config;

import lombok.Data;

@Data
public class CallbackConfig {

    private String callbackSecret;

    private String gatewayUrl;
}
