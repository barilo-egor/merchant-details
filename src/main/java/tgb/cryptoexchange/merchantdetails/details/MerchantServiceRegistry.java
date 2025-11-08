package tgb.cryptoexchange.merchantdetails.details;

import org.springframework.stereotype.Component;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MerchantServiceRegistry {

    private final Map<Merchant, MerchantService> serviceMap;

    public MerchantServiceRegistry(List<MerchantService> services) {
        serviceMap = new EnumMap<>(Merchant.class);
        for (MerchantService service : services) {
            serviceMap.put(service.getMerchant(), service);
        }
    }

    public Optional<MerchantService> getService(Merchant merchant) {
        return Optional.ofNullable(serviceMap.get(merchant));
    }
}
