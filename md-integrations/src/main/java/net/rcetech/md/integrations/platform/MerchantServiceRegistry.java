package net.rcetech.md.integrations.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MerchantServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(MerchantServiceRegistry.class);

    private final Map<Merchant, MerchantService> serviceMap;

    public MerchantServiceRegistry(List<MerchantService> services, MerchantServiceFactory factory) {
        serviceMap = new EnumMap<>(Merchant.class);
        for (MerchantService service : services) {
            serviceMap.put(service.getMerchant(), service);
        }
        // Аккаунты без собственного класса создаются фабрикой из справочника мерчантов.
        // Свой @Service-бин всегда приоритетнее: фабрика вызывается только для незакрытых мерчантов,
        // чтобы не требовать группы конфига у мерчантов, чьи свойства биндятся своими бинами.
        // Отсутствие группы в конфиге означает «аккаунт не сконфигурирован»: сервиса нет,
        // запросы к такому мерчанту вернут «сервис не найден». Старт не должен зависеть
        // от полноты конфига по всем мерчантам справочника.
        for (Merchant merchant : Merchant.values()) {
            if (serviceMap.containsKey(merchant)) {
                continue;
            }
            try {
                factory.create(merchant).ifPresent(service -> serviceMap.put(merchant, service));
            } catch (IllegalStateException e) {
                log.warn("Сервис для мерчанта {} не создан: {}", merchant, e.getMessage());
            }
        }
    }

    public Optional<MerchantService> getService(Merchant merchant) {
        return Optional.ofNullable(serviceMap.get(merchant));
    }
}
