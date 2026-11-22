package net.rcetech.md.app.service;

import net.rcetech.md.domain.entity.SmackPayClientId;
import net.rcetech.md.domain.repository.SmackPayClientIdRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmackPayClientIdServiceTest {

    @Mock
    private SmackPayClientIdRepository repository;

    @InjectMocks
    private SmackPayClientIdService service;

    @Test
    void findOrAssignClientIdShouldReturnExistingClientId() {
        when(repository.findByUserId("u1")).thenReturn(Optional.of(clientId("c1", "u1")));

        assertEquals(Optional.of("c1"), service.findOrAssignClientId("u1"));
        verify(repository, never()).claim(anyString(), anyString());
    }

    @Test
    void findOrAssignClientIdShouldClaimFreeRow() {
        when(repository.findByUserId("u1")).thenReturn(Optional.empty());
        when(repository.findFirstByUserIdIsNullOrderByClientIdAsc())
                .thenReturn(Optional.of(clientId("c1", null)));
        when(repository.claim("c1", "u1")).thenReturn(1);

        assertEquals(Optional.of("c1"), service.findOrAssignClientId("u1"));
    }

    @Test
    void findOrAssignClientIdShouldRetryNextFreeRowWhenClaimLost() {
        when(repository.findByUserId("u1")).thenReturn(Optional.empty());
        when(repository.findFirstByUserIdIsNullOrderByClientIdAsc())
                .thenReturn(Optional.of(clientId("c1", null)),
                            Optional.of(clientId("c2", null)));
        when(repository.claim("c1", "u1")).thenReturn(0);
        when(repository.claim("c2", "u1")).thenReturn(1);

        assertEquals(Optional.of("c2"), service.findOrAssignClientId("u1"));
    }

    @Test
    void findOrAssignClientIdShouldReturnEmptyWhenNoFreeRows() {
        when(repository.findByUserId("u1")).thenReturn(Optional.empty());
        when(repository.findFirstByUserIdIsNullOrderByClientIdAsc()).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service.findOrAssignClientId("u1"));
    }

    @Test
    void findOrAssignClientIdShouldReturnRowAssignedByAnotherInstance() {
        when(repository.findByUserId("u1")).thenReturn(Optional.empty(),
                Optional.of(clientId("c1", "u1")));
        when(repository.findFirstByUserIdIsNullOrderByClientIdAsc())
                .thenReturn(Optional.of(clientId("c2", null)));
        when(repository.claim("c2", "u1")).thenThrow(new DuplicateKeyException("u1 уже записан"));

        assertEquals(Optional.of("c1"), service.findOrAssignClientId("u1"));
    }

    @Test
    void findOrAssignClientIdShouldReturnEmptyForNullUserId() {
        assertEquals(Optional.empty(), service.findOrAssignClientId(null));
    }

    private static SmackPayClientId clientId(String clientId, String userId) {
        return SmackPayClientId.builder()
                .clientId(clientId)
                .userId(userId)
                .build();
    }
}
