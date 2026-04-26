package tgb.cryptoexchange.merchantdetails.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VariableType {
    /**
     * Количество раз, которое будет выполнен поиск реквизитов среди всех мерчантов
     */
    ATTEMPTS_COUNT("1"),
    /**
     * Минимальное время, которое отводится под попытку получения реквизитов у всех мерчантов
     */
    MIN_ATTEMPT_TIME("10");

    private final String defaultValue;
}
