package net.rcetech.md.integrations.platform.bridgepay;

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
    SBP("SBP", "СБП", RequestMethod.SBP),
    TO_CARD("TO_CARD", "Перевод на карту", RequestMethod.CARD),
    CROSS_BORDER("CROSS_BORDER", "Трансграничный перевод", null),
    MOBILE_TOP_UP("MOBILE_TOP_UP", "Пополнение счета моб.телефона", null),
    SBP_QR("SBP_QR", "СБП QR", null),
    MANUAL_SBP_QR("MANUAL_SBP_QR", "Ручной QR", null),
    VIET_QR("VIET_QR", "VIET QR", null);

    @JsonValue
    private final String value;

    private final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

    @JsonCreator
    private static Method fromValue(String value) {
        for (Method method : Method.values()) {
            if (method.getValue().equals(value)) {
                return method;
            }
        }
        return null;
    }
}
