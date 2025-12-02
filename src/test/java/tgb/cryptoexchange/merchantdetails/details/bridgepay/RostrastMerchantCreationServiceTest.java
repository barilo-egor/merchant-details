package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.RostrastProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RostrastMerchantCreationServiceTest {

    @Mock
    private RostrastProperties rostrastProperties;

    @Mock
    private CallbackConfig callbackConfig;

    @InjectMocks
    private RostrastMerchantCreationService rostrastMerchantCreationService;

    @Test
    void getMerchantShouldReturnAlfaTeam() {
        assertEquals(Merchant.ROSTRAST, rostrastMerchantCreationService.getMerchant());
    }

    @CsvSource({
            "https://gateway.paysendmmm.online,b2DVpRm6WXxzBvN",
            "https://bulba.paysendmmm.online,gCQ8DmeRRWb5fVm"
    })
    @ParameterizedTest
    void bodyShouldBuildRequestObject(String gatewayUrl, String secret) {
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setAmount(1000);
        detailsRequest.setMethods(List.of(DetailsRequest.MerchantMethod.builder().merchant(Merchant.ROSTRAST).method("SBP").build()));
        when(callbackConfig.getCallbackSecret()).thenReturn(secret);
        when(callbackConfig.getGatewayUrl()).thenReturn(gatewayUrl);

        when(rostrastProperties.token()).thenReturn("token");

        Request actual = rostrastMerchantCreationService.body(detailsRequest);
        assertEquals(gatewayUrl + "/merchant-details/callback?merchant=ROSTRAST&secret=" + secret,
                actual.getNotificationUrl());
    }
}