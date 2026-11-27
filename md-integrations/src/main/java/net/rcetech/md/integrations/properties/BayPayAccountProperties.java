package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта baypay. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record BayPayAccountProperties(String url, String apiKey) implements BayPayProperties {
}
