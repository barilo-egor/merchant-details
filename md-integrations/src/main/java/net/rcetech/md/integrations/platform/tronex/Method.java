package net.rcetech.md.integrations.platform.tronex;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("c2c", null, "Карта", RequestMethod.CARD),
    SBP("sbp", null, "СБП", RequestMethod.SBP),
    SIM("mc", null, "Sim", null),
    QR("empty", "empty", "QR", null),
    SBER_QR("vietqr", "sber", "Sber QR", null),
    ALFA_QR("vietqr", "alfa", "Alfa QR", null),
    VTB_QR("vietqr", "vtb", "VTB QR", null),
    ALFA_SBP_DL("sbp", "alfa", "СБП Альфа DL", null),
    SBER_SBP_DL("sbp", "sber", "СБП Сбер DL", null),
    T_BANK_CARD_DL("c2c", "tbank", "Карта Т-Банк DL", null),
    OZON_CARD_DL("c2c", "ozon", "Карта Озон DL", null);

    @JsonValue
    final String methodUid;

    final String sourceBank;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    @JsonCreator
    public static Method fromValue(String v) {
        for (Method method : Method.values()) {
            if (method.methodUid.equals(v)) {
                return method;
            }
        }
        return null;
    }
}
