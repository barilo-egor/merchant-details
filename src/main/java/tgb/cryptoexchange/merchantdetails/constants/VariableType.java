package tgb.cryptoexchange.merchantdetails.constants;

import lombok.AllArgsConstructor;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;

@AllArgsConstructor
public enum VariableType {
    /**
     * Количество раз, которое будет выполнен поиск реквизитов среди всех мерчантов
     */
    ATTEMPTS_COUNT("3"),
    /**
     * Минимальное время, которое отводится под попытку получения реквизитов у всех мерчантов
     */
    MIN_ATTEMPT_TIME("15");

    private final String defaultValue;

    public String getDefaultValue(ConfigType type) {
        if (ConfigType.API.equals(type)) {
            return switch (this) {
                case ATTEMPTS_COUNT -> "1";
                case MIN_ATTEMPT_TIME -> "10";
            };
        }
        return defaultValue;
    }
}
