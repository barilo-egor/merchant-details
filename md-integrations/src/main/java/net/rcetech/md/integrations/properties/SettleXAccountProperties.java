package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта settlex. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record SettleXAccountProperties(String url, String key, String sbpId, String c2cId)
        implements SettleXProperties {
}
