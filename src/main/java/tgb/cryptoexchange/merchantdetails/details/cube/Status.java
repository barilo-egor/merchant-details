package tgb.cryptoexchange.merchantdetails.details.cube;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderStatus;

@AllArgsConstructor
@Getter
public enum Status implements MerchantOrderStatus {
    ACCEPTED("Платеж принят"),
    ERROR("Ошибка при обработке"),
    SUCCESS("Платеж успешно завершен"),
    APPEAL("Создана апелляция");

    private final String description;

}
