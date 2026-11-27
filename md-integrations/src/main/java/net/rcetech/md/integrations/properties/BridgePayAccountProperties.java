package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта bridgepay. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record BridgePayAccountProperties(String url, String token, String key, String secret)
        implements BridgePayProperties {
}
