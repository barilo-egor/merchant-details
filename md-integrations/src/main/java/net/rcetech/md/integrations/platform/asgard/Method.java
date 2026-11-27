package net.rcetech.md.integrations.platform.asgard;

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
    CARD("c2c", "Карта", RequestMethod.CARD),
    SBP("sbp", "СБП", RequestMethod.SBP),
    SIM("sim", "Sim", null);

    @JsonValue
    final String methodUid;

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
