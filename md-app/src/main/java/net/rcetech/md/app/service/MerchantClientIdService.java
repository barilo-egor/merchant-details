package net.rcetech.md.app.service;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.domain.entity.MerchantClientId;
import net.rcetech.md.domain.repository.MerchantClientIdRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

/**
 * Соответствия наш id (OrderCreationRequest.userId) -> client_id мерчанта,
 * единая таблица merchant_client_id (md-domain), разбиение по merchant.
 * Строка ищется по user_id, если её нет — под пользователя занимается свободная строка.
 * Устойчиво к одновременной работе нескольких инстансов: claim построчный атомарный,
 * при потере гонки берётся следующая свободная строка или строка, назначенная параллельно.
 */
@Service
@Slf4j
public class MerchantClientIdService {

    private final MerchantClientIdRepository repository;

    public MerchantClientIdService(MerchantClientIdRepository repository) {
        this.repository = repository;
    }

    public Optional<String> findOrAssign(Merchant merchant, String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findByMerchantAndUserId(merchant, userId)
                .map(row -> row.getId().getClientId())
                .or(() -> claimFreeRow(merchant, userId));
    }

    private Optional<String> claimFreeRow(Merchant merchant, String userId) {
        while (true) {
            Optional<MerchantClientId> free =
                    repository.findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc();
            if (free.isEmpty()) {
                log.warn("Для userId {} в merchant_client_id ({}) не осталось свободных строк",
                        userId, merchant);
                return Optional.empty();
            }
            String clientId = free.get().getId().getClientId();
            try {
                if (repository.claim(merchant, clientId, userId) > 0) {
                    return Optional.of(clientId);
                }
            } catch (DuplicateKeyException e) {
                return repository.findByMerchantAndUserId(merchant, userId)
                        .map(row -> row.getId().getClientId());
            }
        }
    }
}
