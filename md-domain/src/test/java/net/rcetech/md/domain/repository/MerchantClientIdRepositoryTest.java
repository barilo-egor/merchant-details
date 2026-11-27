package net.rcetech.md.domain.repository;

import net.rcetech.md.domain.AbstractDataJpaTest;
import net.rcetech.md.domain.entity.MerchantClientId;
import net.rcetech.md.domain.entity.MerchantClientIdId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MerchantClientIdRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private MerchantClientIdRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    private static MerchantClientId clientId(Merchant merchant, String clientId, String userId) {
        return MerchantClientId.builder()
                .id(new MerchantClientIdId(clientId, merchant))
                .userId(userId)
                .build();
    }

    @Test
    void findByMerchantAndUserIdShouldReturnEmptyForUnknownUser() {
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c1", null));

        assertEquals(Optional.empty(), repository.findByMerchantAndUserId(Merchant.CANPAY, "u1"));
    }

    @Test
    void findByMerchantAndUserIdShouldReturnRowByUserId() {
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c1", "u1"));

        Optional<MerchantClientId> found = repository.findByMerchantAndUserId(Merchant.CANPAY, "u1");

        assertTrue(found.isPresent());
        assertEquals("c1", found.get().getId().getClientId());
    }

    @Test
    void sameUserShouldBeAllowedOnDifferentMerchants() {
        repository.saveAndFlush(clientId(Merchant.SMACK_PAY, "s1", "u1"));
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c1", "u1"));

        assertTrue(repository.findByMerchantAndUserId(Merchant.SMACK_PAY, "u1").isPresent());
        assertTrue(repository.findByMerchantAndUserId(Merchant.CANPAY, "u1").isPresent());
    }

    @Test
    void findFirstFreeShouldReturnLowestFreeRow() {
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c1", "u1"));
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c2", null));
        repository.saveAndFlush(clientId(Merchant.CANPAY, "c3", null));

        assertEquals("c2", repository
                .findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc()
                .orElseThrow().getId().getClientId());
    }

    @Test
    void sameClientIdShouldBeAllowedOnDifferentMerchants() {
        repository.saveAndFlush(clientId(Merchant.SMACK_PAY, "shared", "u1"));
        assertDoesNotThrow(() -> repository.saveAndFlush(clientId(Merchant.CANPAY, "shared", "u2")));
    }
}
