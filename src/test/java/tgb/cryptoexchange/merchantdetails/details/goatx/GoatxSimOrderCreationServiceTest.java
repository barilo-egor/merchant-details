package tgb.cryptoexchange.merchantdetails.details.goatx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GoatxSimProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class GoatxSimOrderCreationServiceTest {

    @Mock
    private GoatxSimProperties goatxSimProperties;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GoatxSimMerchantOrderCreationService goatxMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnGoatxSim() {
        assertEquals(Merchant.GOAT_X_SIM, goatxMerchantOrderCreationService.getMerchant());
    }
}
