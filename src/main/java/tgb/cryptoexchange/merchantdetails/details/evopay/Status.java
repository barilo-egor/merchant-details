package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    CREATED("Создана"),
    IN_PROCESS("В процессе"),
    EXPIRE("Истек"),
    SUCCESS("Успешно"),
    CANCEL("Отменен"),
    APPEAL("В споре");

    final String description;
}
