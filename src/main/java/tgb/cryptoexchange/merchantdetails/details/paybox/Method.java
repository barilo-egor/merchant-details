package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("Карта", "/card", null, RequestMethod.CARD),
    SBP("СБП", "/sbp", null, RequestMethod.SBP),
    TRANSGRAN_SBP("Трансгран СБП", "transgran-sbp", null, null),
    QR("QR", "/qr", null, null),
    SBER_QR("Сбер QR", "/internal-qr", "Сбербанк", null),
    VTB_QR("VTB QR", "/internal-qr", "ВТБ", null);

    final String description;

    final String uri;

    final String bankName;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
