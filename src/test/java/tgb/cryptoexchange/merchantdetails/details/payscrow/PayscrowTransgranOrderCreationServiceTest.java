package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PayscrowTransgranOrderCreationServiceTest {

    @InjectMocks
    private PayscrowTransgranOrderCreationService service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals(
                "/api/v1/form/create",
                service.uriBuilder(null).apply(uriBuilder).getPath()
        );
    }

    @Test
    void getMerchantShouldReturnPayscrowLow() {
        assertEquals(Merchant.PAYSCROW_TRANSGRAN, service.getMerchant());
    }
}