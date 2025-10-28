package tgb.cryptoexchange.merchantdetails.details.creation.whitelabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.GeoTransferProperties;
import tgb.cryptoexchange.merchantdetails.service.SignatureService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GeoTransferMerchantCreationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private GeoTransferProperties geoTransferProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SignatureService signatureService;

    @InjectMocks
    private GeoTransferMerchantCreationService geoTransferMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.GEO_TRANSFER, geoTransferMerchantCreationService.getMerchant());
    }
}