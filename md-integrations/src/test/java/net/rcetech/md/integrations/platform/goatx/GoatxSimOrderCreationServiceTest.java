package net.rcetech.md.integrations.platform.goatx;

import org.springframework.web.client.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.integrations.properties.GoatxSimProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class GoatxSimOrderCreationServiceTest {

    @Mock
    private GoatxSimProperties goatxSimProperties;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private GoatxSimMerchantOrderCreationService goatxMerchantOrderCreationService;

    @Test
    void getMerchantShouldReturnGoatxSim() {
        assertEquals(Merchant.GOAT_X_SIM, goatxMerchantOrderCreationService.getMerchant());
    }
}
