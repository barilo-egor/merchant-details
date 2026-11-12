package tgb.cryptoexchange.merchantdetails.details.smackpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {

    QR("QR", "", null),
    NSPK_QR("NSPK QR", "/qr-kyc", null);

    private final String description;

    private final String url;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

}
