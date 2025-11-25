package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.details.UnwrappedCallback;

@EqualsAndHashCode(callSuper = true)
@Data
public class Callback extends UnwrappedCallback {

    private String id;

    private Status status;
}
