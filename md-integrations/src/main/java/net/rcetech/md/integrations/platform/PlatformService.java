package net.rcetech.md.integrations.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

/**
 * Операции над ордером конкретной площадки, не связанные с поиском реквизитов.
 */
@Service
@Slf4j
public class PlatformService {

    private final MerchantServiceRegistry merchantServiceRegistry;

    public PlatformService(MerchantServiceRegistry merchantServiceRegistry) {
        this.merchantServiceRegistry = merchantServiceRegistry;
    }

    /**
     * Отменяет ордер на площадке.
     *
     * @param merchant           площадка
     * @param cancelOrderRequest данные ордера для отмены
     */
    public void cancelOrder(Merchant merchant, CancelOrderRequest cancelOrderRequest) {
        log.debug("Запрос на отмену ордера мерчанта {}: {}", merchant.name(), cancelOrderRequest.toString());
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isPresent()) {
            maybeMerchantService.get().cancelOrder(cancelOrderRequest);
        } else {
            log.warn("Запрос отмены ордера мерчанта {}, у которого отсутствует реализация: {}",
                    merchant.name(), cancelOrderRequest);
        }
    }

    /**
     * Отправляет площадке чек по ордеру.
     *
     * @param merchant площадка
     * @param orderId  идентификатор ордера на стороне площадки
     * @param file     файл чека
     */
    public void sendReceipt(Merchant merchant, String orderId, MultipartFile file) {
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isPresent()) {
            maybeMerchantService.get().sendReceipt(orderId, file);
        } else {
            log.debug("Отсутствует реализация для мерчанта {}. Чек по ордеру {} отправлен не будет", merchant.name(), orderId);
        }
    }

    /**
     * Отправляет площадке чек по ордеру.
     *
     * @param merchant    площадка
     * @param orderId     идентификатор ордера на стороне площадки
     * @param fileContent содержимое файла чека
     * @param fileName    имя файла чека
     */
    public void sendReceipt(Merchant merchant, String orderId, byte[] fileContent, String fileName) {
        Optional<MerchantService> maybeMerchantService = merchantServiceRegistry.getService(merchant);
        if (maybeMerchantService.isPresent()) {
            maybeMerchantService.get().sendReceipt(orderId, fileContent, fileName);
        } else {
            log.debug("Отсутствует реализация для мерчанта {}. Чек по ордеру {} отправлен не будет", merchant.name(), orderId);
        }
    }
}
