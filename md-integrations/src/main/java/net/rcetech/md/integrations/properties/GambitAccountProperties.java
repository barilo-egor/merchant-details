package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта gambit/hesoyam. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 */
public record GambitAccountProperties(String url, String key, String terminal) implements GambitProperties {
}
