package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта payscrow. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record PayscrowAccountProperties(String url, String key) implements PayscrowProperties {
}
