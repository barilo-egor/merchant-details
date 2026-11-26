package net.rcetech.md.app.service;

import lombok.extern.slf4j.Slf4j;
import net.rcetech.md.domain.entity.CanPayClientId;
import net.rcetech.md.domain.repository.CanPayClientIdRepository;
import net.rcetech.md.integrations.platform.canpay.CanPayClientIds;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Соответствия наш id (OrderCreationRequest.userId) -> client_id, отправляемый в CanPay как client_id,
 * в таблице can_pay_client_id.
 */
@Service
@Slf4j
public class CanPayClientIdService implements CanPayClientIds {

    private final CanPayClientIdRepository repository;

    public CanPayClientIdService(CanPayClientIdRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> findOrAssignClientId(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findByUserId(userId)
                .map(CanPayClientId::getClientId)
                .or(() -> claimFreeRow(userId));
    }

    private Optional<String> claimFreeRow(String userId) {
        while (true) {
            Optional<CanPayClientId> free = repository.findFirstByUserIdIsNullOrderByClientIdAsc();
            if (free.isEmpty()) {
                log.warn("Для userId {} в can_pay_client_id не осталось свободных строк", userId);
                return Optional.empty();
            }
            String clientId = free.get().getClientId();
            try {
                if (repository.claim(clientId, userId) > 0) {
                    return Optional.of(clientId);
                }
            } catch (DuplicateKeyException e) {
                return repository.findByUserId(userId).map(CanPayClientId::getClientId);
            }
        }
    }
}
