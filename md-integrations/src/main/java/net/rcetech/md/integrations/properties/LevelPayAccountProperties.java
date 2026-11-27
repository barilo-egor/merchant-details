package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта levelpay. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record LevelPayAccountProperties(String url, String token, String merchantId)
        implements LevelPayProperties {
}
