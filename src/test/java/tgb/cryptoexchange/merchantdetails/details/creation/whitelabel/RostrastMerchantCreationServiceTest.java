package tgb.cryptoexchange.merchantdetails.details.creation.whitelabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.RostrastProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RostrastMerchantCreationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private RostrastProperties rostrastProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private RostrastMerchantCreationService rostrastMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ROSTRAST, rostrastMerchantCreationService.getMerchant());
    }
}