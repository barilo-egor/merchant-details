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
    TRANSGRAN_SBP("Трансгран СБП", "/transgran-sbp", null, null),
    QR("QR", "/qr", null, null),
    SBER_QR("Сбер QR", "/internal-qr", "Сбербанк", null),
    VTB_QR("VTB QR", "/internal-qr", "ВТБ", null),
    VIET_QR("Вьетнам QR", "/viet-qr", null, null),
    T_BANK_TRANS_CARD_DL("Трансгран карта Т-Банк DL", "/transgran-card-tbank", null, null),
    VTB_TRANS_SBP_DL("Трансгран СБП ВТБ DL", "/transgran-sbp-vtb", null, null);

    final String description;

    final String uri;

    final String bankName;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
