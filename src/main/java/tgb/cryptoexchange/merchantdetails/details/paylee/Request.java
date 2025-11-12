package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    private Integer price;

    @JsonSerialize(using = Method.Serializer.class)
    private Method requisitesType;
}
