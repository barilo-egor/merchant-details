package tgb.cryptoexchange.merchantdetails.details.crocopay;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Transaction {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;
}
