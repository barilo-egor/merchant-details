package tgb.cryptoexchange.merchantdetails.details.pspware;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

@Data
public class Request {

    private Integer sum;

    @JsonSerialize(contentUsing = Method.Serializer.class)
    @JsonProperty("pay_types")
    private List<Method> payTypes;

    private List<String> geos;
}
