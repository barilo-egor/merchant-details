package net.rcetech.md.integrations.platform.bitzone;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Request {

    private Method method;

    private Integer fiatAmount;

    private Extra extra;

    private String callbackUrl;

    @Data
    @AllArgsConstructor
    public static class Extra {

        private String externalTransactionId;
    }
}
