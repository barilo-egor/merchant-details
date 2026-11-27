package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта tronex. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 * У групп tron-ex-bt/qr/pdf нет url — RestClient строится по url группы tron-ex-sim.
 */
public record TronExAccountProperties(String url, String dlUrl, String key) implements TronExProperties {
}
