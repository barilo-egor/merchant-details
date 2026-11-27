package net.rcetech.md.integrations.platform.rspay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {
    SBP("sbp", "СБП", RequestMethod.SBP),
    CARD("card", "Перевод на карту", RequestMethod.CARD),
    QR("sberbank_qr_vnm", "QR", null),
    SIM("mobile_number ", "Sim", null);

    @JsonValue
    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    @JsonCreator
    public static Method fromValue(String v) {
        for (Method method : Method.values()) {
            if (method.value.equals(v)) {
                return method;
            }
        }
        return null;
    }

}
