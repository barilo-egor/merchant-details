package tgb.cryptoexchange.merchantdetails.details.goatx;

import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    private String contract;

    private String invid;

    private String sum;

    private String signature;

    @JsonSerialize(using = Method.Serializer.class)
    private Method way;
}
