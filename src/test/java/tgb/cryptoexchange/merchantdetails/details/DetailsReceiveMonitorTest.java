package tgb.cryptoexchange.merchantdetails.details;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DetailsReceiveMonitorTest {

    @Test
    void startShouldThrowUnsupportedOperationExceptionIfTryStartAfterStop() {
        DetailsReceiveMonitor detailsReceiveMonitor = new DetailsReceiveMonitor(1L, 1);
        assertDoesNotThrow(() -> detailsReceiveMonitor.start(Merchant.ALFA_TEAM, "SBP"));
        detailsReceiveMonitor.stop(true);
        assertThrows(UnsupportedOperationException.class, () -> detailsReceiveMonitor.start(Merchant.ALFA_TEAM, "SBP"));
    }

}