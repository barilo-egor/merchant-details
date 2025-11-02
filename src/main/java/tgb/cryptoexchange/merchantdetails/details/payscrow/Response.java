package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("method_name")
    private String methodName;

    @JsonProperty("holder_account")
    private String holderAccount;
}
