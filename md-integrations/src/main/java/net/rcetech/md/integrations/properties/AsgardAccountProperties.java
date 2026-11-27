package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта asgard. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record AsgardAccountProperties(String url, String token, String secret, String merchantId)
        implements AsgardProperties {
}
