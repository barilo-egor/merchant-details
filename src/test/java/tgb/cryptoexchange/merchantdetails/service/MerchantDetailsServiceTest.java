package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantService;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.kafka.MerchantDetailsReceiveEventProducer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantDetailsServiceTest {

    @Mock
    private MerchantServiceRegistry merchantServiceRegistry;

    @Mock
    private MerchantDetailsReceiveEventProducer merchantDetailsReceiveEventProducer;

    @InjectMocks
    private MerchantDetailsService merchantDetailsService;

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantServiceNotImplemented() {
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, new DetailsRequest()).isEmpty());
    }

    @Test
    void getDetailsShouldReturnEmptyOptionalIfMerchantReturnNoDetails() {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        when(merchantService.createOrder(any())).thenReturn(Optional.empty());
        assertTrue(merchantDetailsService.getDetails(Merchant.ALFA_TEAM, new DetailsRequest()).isEmpty());
    }

    @Test
    void getDetailsShouldReturnDetailsAndPutDetailsToProducer() {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(any())).thenReturn(Optional.of(merchantService));
        DetailsRequest detailsRequest = new DetailsRequest();
        detailsRequest.setId(50005L);
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(Merchant.ALFA_TEAM);
        detailsResponse.setDetails("SOME BANK 1234 1234 1234 1234");
        when(merchantService.createOrder(any())).thenReturn(Optional.of(detailsResponse));
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        ArgumentCaptor<DetailsRequest> detailsRequestCaptor = ArgumentCaptor.forClass(DetailsRequest.class);
        ArgumentCaptor<DetailsResponse> detailsResponseCaptor = ArgumentCaptor.forClass(DetailsResponse.class);
        Optional<DetailsResponse> maybeResponse = merchantDetailsService.getDetails(Merchant.ALFA_TEAM, detailsRequest);
        assertTrue(maybeResponse.isPresent());
        DetailsResponse actual = maybeResponse.get();
        assertEquals(Merchant.ALFA_TEAM, actual.getMerchant());
        verify(merchantDetailsReceiveEventProducer).put(merchantCaptor.capture(), detailsRequestCaptor.capture(), detailsResponseCaptor.capture());
        assertAll(
                () -> assertEquals(Merchant.ALFA_TEAM, merchantCaptor.getValue()),
                () -> assertEquals(detailsRequest, detailsRequestCaptor.getValue()),
                () -> assertEquals(detailsResponse, detailsResponseCaptor.getValue())
        );
    }

    @CsvSource(delimiter = ';', textBlock = """
            ALFA_TEAM;{"someField":"someValue","merchant":"ALFA_TEAM"}
            WELL_BIT;{"someField":"someValue","merchant":"WELL_BIT"}
            """)
    @ParameterizedTest
    void updateStatusShouldCallMerchantServiceUpdateStatusMethod(Merchant merchant, String body) {
        MerchantService merchantService = Mockito.mock(MerchantService.class);
        when(merchantServiceRegistry.getService(merchant)).thenReturn(Optional.of(merchantService));
        merchantDetailsService.updateStatus(merchant, body);
        verify(merchantService).updateStatus(body);

    }
}