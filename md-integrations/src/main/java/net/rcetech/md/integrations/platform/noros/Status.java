package net.rcetech.md.integrations.platform.noros;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("created", "Создана"),
    OFFER("offer", "Предложение"),
    ACCEPTED("accepted", "Принята"),
    PROOF("proof", "Доказательство"),
    COMPLETED("completed", "Подтверждена"),
    CANCELED("canceled", "Отменена");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        for (Status c: Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

}
