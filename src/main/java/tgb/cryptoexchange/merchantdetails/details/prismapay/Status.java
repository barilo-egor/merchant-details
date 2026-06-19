package tgb.cryptoexchange.merchantdetails.details.prismapay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    PENDING("Создана"),
    CANCELLED("Отменено"),
    DISPUTE("В споре"),
    SUCCESS("Успешно"),
    WAITING_ADMIN_APPROVAL("Ожидает одобрения администратора"),
    EXPIRED("Ожидает одобрения администратора"),
    UPDATED("Просрочена");

    final String description;
}
