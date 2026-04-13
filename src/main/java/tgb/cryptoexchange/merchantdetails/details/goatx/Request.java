package tgb.cryptoexchange.merchantdetails.details.goatx;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private String contract;

    private String invid;

    private String sum;

    private String signature;

    @JsonSerialize(using = Method.Serializer.class)
    private Method way;
}
