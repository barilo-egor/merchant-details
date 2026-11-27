package net.rcetech.md.integrations.properties;

/**
 * Универсальные свойства аккаунта rs-pay-bt. Привязываются из группы конфига
 * по префиксу из MerchantCatalog — отдельный properties-класс на аккаунт не нужен.
 * Группа rs-pay-bt не содержит url: RestClient строится по url группы rs-pay
 * (так же работал общий бин rsPayRestClient), поэтому url() всегда null.
 */
public record RSPayBTAccountProperties(String apiKey, String secret) implements RSPayProperties {

    @Override
    public String url() {
        return null;
    }
}
