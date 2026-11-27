package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта manypay. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record ManyPayAccountProperties(String url, String token) implements ManyPayProperties {
}
