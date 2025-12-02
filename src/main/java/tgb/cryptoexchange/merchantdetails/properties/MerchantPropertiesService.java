package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static tgb.cryptoexchange.merchantdetails.enums.Merchant.EVO_PAY;

/**
 * Класс создан на время трансфера сервиса из монолита
 */
@Service
public class MerchantPropertiesService {

    private final Map<Merchant, Object> properties;

    public MerchantPropertiesService(EvoPayProperties evoPayProperties) {
        properties = new EnumMap<>(Merchant.class);
        properties.put(EVO_PAY, evoPayProperties);
    }

    public Optional<Object> getProperties(Merchant merchant) {
        Object merchantProperties = properties.get(merchant);
        return Optional.ofNullable(merchantProperties);
    }
}
