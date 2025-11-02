package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private Integer id;

    private String requisites;

    private String bankName;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Double price;
}
