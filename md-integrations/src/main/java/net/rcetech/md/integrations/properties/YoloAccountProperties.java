package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта yolo. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record YoloAccountProperties(String accountId, String url, String storeKey, Credentials credentials)
        implements YoloProperties {
}
