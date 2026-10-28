package tgb.cryptoexchange.merchantdetails.detailsapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Категории методов оплаты (методов запроса реквизитов).
 * <p>
 * У разных мерчантов однотипные методы оплаты могут называться по-разному.
 * Данное перечисление используется для агрегации и унификации этих методов.
 * Это позволяет абстрагироваться от специфики конкретных интеграций: при запросе
 * реквизитов указывается одна общая категория вместо перечисления кодов всех мерчантов.
 */
@AllArgsConstructor
@Getter
public enum RequestMethod {
    CARD("Карта"),
    SBP("СБП");

    private final String description;
}
