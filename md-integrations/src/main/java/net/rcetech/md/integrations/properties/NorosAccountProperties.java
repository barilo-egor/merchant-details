package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта noros. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record NorosAccountProperties(String url, String key, String clientIdSalt) implements NorosProperties {
}
