package net.rcetech.md.domain.repository;

import net.rcetech.md.domain.entity.MerchantClientId;
import net.rcetech.md.domain.entity.MerchantClientIdId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;

@Repository
public interface MerchantClientIdRepository extends JpaRepository<MerchantClientId, MerchantClientIdId> {

    Optional<MerchantClientId> findByMerchantAndUserId(Merchant merchant, String userId);

    Optional<MerchantClientId> findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc();

    @Modifying
    @Transactional
    @Query("update MerchantClientId c set c.userId = :userId "
            + "where c.merchant = :merchant and c.id.clientId = :clientId and c.userId is null")
    int claim(@Param("merchant") Merchant merchant, @Param("clientId") String clientId,
              @Param("userId") String userId);
}
