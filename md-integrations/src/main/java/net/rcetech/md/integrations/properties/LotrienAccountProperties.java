package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта lotrien. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 * У LOTRIEN и LOTRIEN_PDF общий префикс lotrien; LOTRIEN_PDF живёт собственным классом.
 */
public record LotrienAccountProperties(String url, String key) implements LotrienProperties {
}
