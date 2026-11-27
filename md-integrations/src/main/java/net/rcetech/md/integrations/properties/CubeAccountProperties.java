package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта cube. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record CubeAccountProperties(String url, String key, String privateKey)
        implements CubeProperties {
}
