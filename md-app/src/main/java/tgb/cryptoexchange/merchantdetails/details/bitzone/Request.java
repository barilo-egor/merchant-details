package tgb.cryptoexchange.merchantdetails.details.bitzone;

import lombok.AllArgsConstructor;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    @JsonSerialize(using = Method.Serializer.class)
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
