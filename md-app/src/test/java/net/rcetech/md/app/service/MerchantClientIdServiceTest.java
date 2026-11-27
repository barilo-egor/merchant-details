package net.rcetech.md.app.service;

import net.rcetech.md.domain.entity.MerchantClientId;
import net.rcetech.md.domain.entity.MerchantClientIdId;
import net.rcetech.md.domain.repository.MerchantClientIdRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantClientIdServiceTest {

    @Mock
    private MerchantClientIdRepository repository;

    @InjectMocks
    private MerchantClientIdService service;

    static Stream<Merchant> merchants() {
        return Stream.of(Merchant.SMACK_PAY, Merchant.CANPAY);
    }

    private static MerchantClientId clientId(Merchant merchant, String clientId, String userId) {
        return MerchantClientId.builder()
                .id(new MerchantClientIdId(clientId, merchant))
                .userId(userId)
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = Merchant.class, names = {"SMACK_PAY", "CANPAY"})
    void findOrAssignShouldReturnExistingClientId(Merchant merchant) {
        when(repository.findByMerchantAndUserId(merchant, "u1"))
                .thenReturn(Optional.of(clientId(merchant, "c1", "u1")));

        assertEquals(Optional.of("c1"), service.findOrAssign(merchant, "u1"));
        verify(repository, never()).claim(any(), anyString(), anyString());
    }

    @ParameterizedTest
    @EnumSource(value = Merchant.class, names = {"SMACK_PAY", "CANPAY"})
    void findOrAssignShouldClaimFreeRow(Merchant merchant) {
        when(repository.findByMerchantAndUserId(merchant, "u1")).thenReturn(Optional.empty());
        when(repository.findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc())
                .thenReturn(Optional.of(clientId(merchant, "c1", null)));
        when(repository.claim(merchant, "c1", "u1")).thenReturn(1);

        assertEquals(Optional.of("c1"), service.findOrAssign(merchant, "u1"));
    }

    @ParameterizedTest
    @EnumSource(value = Merchant.class, names = {"SMACK_PAY", "CANPAY"})
    void findOrAssignShouldRetryNextFreeRowWhenClaimLost(Merchant merchant) {
        when(repository.findByMerchantAndUserId(merchant, "u1")).thenReturn(Optional.empty());
        when(repository.findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc())
                .thenReturn(Optional.of(clientId(merchant, "c1", null)),
                        Optional.of(clientId(merchant, "c2", null)));
        when(repository.claim(merchant, "c1", "u1")).thenReturn(0);
        when(repository.claim(merchant, "c2", "u1")).thenReturn(1);

        assertEquals(Optional.of("c2"), service.findOrAssign(merchant, "u1"));
    }

    @ParameterizedTest
    @EnumSource(value = Merchant.class, names = {"SMACK_PAY", "CANPAY"})
    void findOrAssignShouldReturnEmptyWhenNoFreeRows(Merchant merchant) {
        when(repository.findByMerchantAndUserId(merchant, "u1")).thenReturn(Optional.empty());
        when(repository.findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc()).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service.findOrAssign(merchant, "u1"));
    }

    @ParameterizedTest
    @EnumSource(value = Merchant.class, names = {"SMACK_PAY", "CANPAY"})
    void findOrAssignShouldReturnRowAssignedByAnotherInstance(Merchant merchant) {
        when(repository.findByMerchantAndUserId(merchant, "u1")).thenReturn(Optional.empty(),
                Optional.of(clientId(merchant, "c1", "u1")));
        when(repository.findFirstByMerchantAndUserIdIsNullOrderByIdClientIdAsc())
                .thenReturn(Optional.of(clientId(merchant, "c2", null)));
        when(repository.claim(merchant, "c2", "u1")).thenThrow(new DuplicateKeyException("u1 уже записан"));

        assertEquals(Optional.of("c1"), service.findOrAssign(merchant, "u1"));
    }

    @Test
    void findOrAssignShouldReturnEmptyForNullUserId() {
        assertEquals(Optional.empty(), service.findOrAssign(Merchant.CANPAY, null));
        assertEquals(Optional.empty(), service.findOrAssign(Merchant.SMACK_PAY, null));
    }
}
