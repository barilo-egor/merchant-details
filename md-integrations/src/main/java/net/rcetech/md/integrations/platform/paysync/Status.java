package net.rcetech.md.integrations.platform.paysync;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PAID("paid", "Оплачено"),
    WAIT("wait", "Ожидание"),
    FAILED("failed", "Платеж не удался"),
    PENDING("pending", "Создано");

    private final String value;

    private final String description;

    @JsonCreator
    public static Status fromValue(String v) {
        for (Status c : Status.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
