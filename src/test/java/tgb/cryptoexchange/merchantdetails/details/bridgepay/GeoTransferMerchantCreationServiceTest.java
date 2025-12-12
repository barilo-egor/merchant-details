package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.properties.GeoTransferProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoTransferMerchantCreationServiceTest {

    @Mock
    private GeoTransferProperties geoTransferProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private GeoTransferMerchantCreationService geoTransferMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.GEO_TRANSFER, geoTransferMerchantCreationService.getMerchant());
    }

    @CsvSource({
            "https://gateway.paysendmmm.online,b2DVpRm6WXxzBvN",
            "https://bulba.paysendmmm.online,gCQ8DmeRRWb5fVm"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(String gatewayUrl, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.GEO_TRANSFER).method("SBP").build()));
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(geoTransferProperties.token()).thenReturn("token");

        Request actual = geoTransferMerchantCreationService.body(detailsRequest);
        assertEquals(gatewayUrl + "/merchant-details/callback?merchant=GEO_TRANSFER&secret=" + secret,
                actual.getNotificationUrl());
    }
}