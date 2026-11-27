package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта paybox. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record PayBoxAccountProperties(String url, String token, String signKey)
        implements PayBoxProperties {
}
