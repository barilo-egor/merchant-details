package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта paylee. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record PayLeeAccountProperties(String url, String token, String secret, String clientIdSalt)
        implements PayLeeProperties {
}
