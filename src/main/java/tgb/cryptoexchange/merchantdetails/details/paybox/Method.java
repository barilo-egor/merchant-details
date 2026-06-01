package tgb.cryptoexchange.merchantdetails.details.paybox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    CARD("Карта", "/card", null),
    SBP("СБП", "/sbp", null),
    TRANSGRAN_SBP("Трансгран СБП", "transgran-sbp", null),
    QR("QR", "/qr", null),
    SBER_QR("SBER QR", "/internal-qr", "Сбербанк"),
    VTB_QR("VTB QR", "/internal-qr", "ВТБ");

    final String description;

    final String uri;

    final String bankName;
}
