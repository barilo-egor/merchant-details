package tgb.cryptoexchange.merchantdetails.details.paycrown;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    NEW("new", "Новый"),
    OPENED("opened", "Открыт"),
    PROCESS("process", "В процессе"),
    CANCELED("canceled", "Заказ отменен"),
    EXPIRED("expired", "Срок истек"),
    CLOSED("closed", "Успешно исполнен");

    private final String value;

    private final String description;

    public static Status fromValue(String value) {
        for (Status status : Status.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
