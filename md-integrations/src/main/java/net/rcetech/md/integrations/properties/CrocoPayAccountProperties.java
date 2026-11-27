package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта crocopay. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record CrocoPayAccountProperties(String url, String clientId, String clientSecret)
        implements CrocoPayProperties {
}
