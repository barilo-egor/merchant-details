package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.Optional;

@Service
@Slf4j
public class MerchantDetailsService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    public MerchantDetailsService(MerchantServiceRegistry merchantServiceRegistry) {
        this.merchantServiceRegistry = merchantServiceRegistry;
    }

    public Optional<DetailsResponse> getDetails(Merchant merchant, DetailsRequest request) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            return maybeCreationService.get().createOrder(request);
        }
        log.warn("Запрос получения реквизитов мерчанта {}, у которого отсутствует реализация: {}", merchant.name(), request.toString());
        return Optional.empty();
    }
}
