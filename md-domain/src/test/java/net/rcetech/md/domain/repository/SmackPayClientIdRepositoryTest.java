package net.rcetech.md.domain.repository;

import net.rcetech.md.domain.AbstractDataJpaTest;
import net.rcetech.md.domain.entity.SmackPayClientId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmackPayClientIdRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private SmackPayClientIdRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByUserIdShouldReturnEmptyForUnknownUser() {
        repository.saveAndFlush(clientId("c1", null));

        assertEquals(Optional.empty(), repository.findByUserId("u1"));
    }

    @Test
    void findByUserIdShouldReturnRowByUserId() {
        repository.saveAndFlush(clientId("c1", "u1"));

        Optional<SmackPayClientId> found = repository.findByUserId("u1");

        assertTrue(found.isPresent());
        assertEquals("c1", found.get().getClientId());
    }

    @Test
    void findFirstFreeShouldReturnLowestFreeRow() {
        repository.saveAndFlush(clientId("c1", "u1"));
        repository.saveAndFlush(clientId("c2", null));
        repository.saveAndFlush(clientId("c3", null));

        assertEquals("c2", repository.findFirstByUserIdIsNullOrderByClientIdAsc().orElseThrow().getClientId());
    }

    @Test
    void findFirstFreeShouldReturnEmptyWhenNoFreeRows() {
        repository.saveAndFlush(clientId("c1", "u1"));

        assertEquals(Optional.empty(), repository.findFirstByUserIdIsNullOrderByClientIdAsc());
    }

    @Test
    void claimShouldAssignFreeRowToUser() {
        repository.saveAndFlush(clientId("c1", null));

        assertEquals(1, repository.claim("c1", "u1"));
        // claim — bulk-операция, сущность в persistence context устарела
        testEntityManager.clear();
        assertEquals("u1", repository.findById("c1").orElseThrow().getUserId());
    }

    @Test
    void claimShouldNotTakeRowAlreadyAssignedToAnotherUser() {
        repository.saveAndFlush(clientId("c1", "u2"));

        assertEquals(0, repository.claim("c1", "u1"));
        testEntityManager.clear();
        assertEquals("u2", repository.findById("c1").orElseThrow().getUserId());
    }

    @Test
    void claimShouldNotTouchOtherRows() {
        repository.saveAndFlush(clientId("c1", null));
        repository.saveAndFlush(clientId("c2", null));

        assertEquals(1, repository.claim("c2", "u2"));

        testEntityManager.clear();
        assertNull(repository.findById("c1").orElseThrow().getUserId());
        assertEquals("u2", repository.findById("c2").orElseThrow().getUserId());
    }

    @Test
    void userIdShouldBeUnique() {
        repository.saveAndFlush(clientId("c1", "u1"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(clientId("c2", "u1")));
    }

    private static SmackPayClientId clientId(String clientId, String userId) {
        return SmackPayClientId.builder()
                .clientId(clientId)
                .userId(userId)
                .build();
    }
}
