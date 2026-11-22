package net.rcetech.md.domain.repository;

import net.rcetech.md.domain.entity.SmackPayClientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SmackPayClientIdRepository extends JpaRepository<SmackPayClientId, String> {

    Optional<SmackPayClientId> findByUserId(String userId);

    Optional<SmackPayClientId> findFirstByUserIdIsNullOrderByClientIdAsc();

    @Modifying
    @Transactional
    @Query("update SmackPayClientId c set c.userId = :userId where c.clientId = :clientId and c.userId is null")
    int claim(@Param("clientId") String clientId, @Param("userId") String userId);
}
