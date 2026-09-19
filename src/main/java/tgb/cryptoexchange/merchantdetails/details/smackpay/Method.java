package tgb.cryptoexchange.merchantdetails.details.smackpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {

    NSPK_QR("NSPK QR");

    private final String description;

}
