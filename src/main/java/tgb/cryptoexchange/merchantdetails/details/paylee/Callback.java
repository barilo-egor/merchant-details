package tgb.cryptoexchange.merchantdetails.details.paylee;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {
    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;
}
