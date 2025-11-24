package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Создана"),
    IN_PROCESS("В процессе"),
    EXPIRE("Истек"),
    SUCCESS("Успешно"),
    CANCEL("Отменен"),
    APPEAL("В споре");

    final String description;
}
