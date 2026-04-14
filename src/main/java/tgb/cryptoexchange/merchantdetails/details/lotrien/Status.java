package tgb.cryptoexchange.merchantdetails.details.lotrien;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    CREATED("Создана"),
    PENDING("В ожидании"),
    IN_PROGRESS("В процессе"),
    SUCCESS("Успешно"),
    UNISSUED("Не создана"),
    EXPIRE("Просрочена"),
    APPEAL("В споре"),
    CANCEL("Отменена");

    final String description;
}
