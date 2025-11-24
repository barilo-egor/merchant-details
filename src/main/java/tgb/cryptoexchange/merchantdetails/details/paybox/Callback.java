package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    @JsonProperty("id")
    private Long responseId;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @Override
    public String getId() {
        return Objects.nonNull(responseId) ? responseId.toString() : null;
    }
}
