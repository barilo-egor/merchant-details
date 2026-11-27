package net.rcetech.md.integrations.platform.wat;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Method implements MerchantMethod {
    CARD("card", "Карта", RequestMethod.CARD),
    SBP("sbp", "СБП", RequestMethod.SBP),
    SIM("sim", "Sim", null),
    BT_PDF("bt-pdf", "BT PDF карта", null),
    BT_PDF_SBP("bt-pdf-sbp", "BT PDF СБП", null);

    @JsonValue
    final String methodUid;

    final String description;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }
}
