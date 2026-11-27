package net.rcetech.md.integrations.platform;

import org.springframework.stereotype.Component;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MerchantServiceRegistry {

    private final Map<Merchant, MerchantService> serviceMap;

    public MerchantServiceRegistry(List<MerchantService> services, MerchantServiceFactory factory) {
        serviceMap = new EnumMap<>(Merchant.class);
        for (MerchantService service : services) {
            serviceMap.put(service.getMerchant(), service);
        }
        // Аккаунты без собственного класса создаются фабрикой из справочника мерчантов.
        for (Merchant merchant : Merchant.values()) {
            factory.create(merchant).ifPresent(service -> serviceMap.putIfAbsent(merchant, service));
        }
    }

    public Optional<MerchantService> getService(Merchant merchant) {
        return Optional.ofNullable(serviceMap.get(merchant));
    }
}
