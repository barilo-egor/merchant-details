package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта goatx. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record GoatxAccountProperties(String url, String apiKey, String merchantId, String merchantContractId,
                                     String login) implements GoatxProperties {
}
