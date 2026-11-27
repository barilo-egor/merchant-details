package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта wat. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 * У групп wat-sim/wat-pdf нет url — RestClient строится по url группы wat.
 */
public record WatAccountProperties(String url, String token) implements WatProperties {
}
