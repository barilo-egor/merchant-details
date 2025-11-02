package tgb.cryptoexchange.merchantdetails.details.pspware;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private String bankName;

    private String card;
}
