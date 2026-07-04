package tgb.cryptoexchange.merchantdetails.details.manypay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.ManyPayPropertiesImpl;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManyPayOrderCreationServiceTest {

    @Mock
    private ManyPayPropertiesImpl manyPayProperties;

    @InjectMocks
    private ManyPayOrderCreationServiceImpl service;

    @Test
    void uriBuilderShouldAddPath() {
        UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
        assertEquals("/api/merchant/order", service.uriBuilder(null).apply(uriBuilder).getPath());
    }

    @ValueSource(strings = {
            "qweqwfqwgqw", "qwrwqrt45qwfr", "ggggggqwtttggweg3"
    })
    @ParameterizedTest
    void headersShouldAddRequiredHeaders(String key) {
        when(manyPayProperties.token()).thenReturn(key);
        HttpHeaders headers = new HttpHeaders();
        service.headers(null, null).accept(headers);
        assertAll(
                () -> assertEquals(key, headers.getFirst("X-API-ACCESS-TOKEN")),
                () -> assertEquals("application/json", headers.getFirst("Content-Type"))
        );
    }

    @Test
    void shouldMapDetailsRequestToRequestBody() {
        DetailsRequest request = mock(DetailsRequest.class);
        when(request.getAmount()).thenReturn(150);
        when(request.getCurrentMerchantMethod()).thenReturn("CARD");
        Request resultBody = service.body(request);

        assertThat(resultBody).isNotNull();
        assertThat(resultBody.getAmount()).isEqualTo(15000);
        assertThat(resultBody.getPaymentMethod()).isNotNull();
    }

    @Test
    void shouldMapResponseToDetailsResponse() {
        Response response = new Response();
        Response.Data data = new Response.Data();
        Response.Requisites requisites = new Response.Requisites();

        requisites.setBankName("Sberbank");
        requisites.setDetails("44445555");
        data.setOrderId("order-id-999");

        data.setStatus(Status.PENDING);
        data.setPaymentDetails(requisites);
        response.setData(data);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertThat(result).isPresent();
        DetailsResponse detailsResponse = result.get();

        assertThat(detailsResponse.getDetails()).isEqualTo("Sberbank 44445555");
        assertThat(detailsResponse.getMerchantOrderId()).isEqualTo("order-id-999");
        assertThat(detailsResponse.getMerchantOrderStatus()).isEqualTo("PENDING");
        assertThat(detailsResponse.getMerchant()).isEqualTo(service.getMerchant());
    }

    @Test
    void getMerchantShouldReturnSettleX() {
        assertEquals(Merchant.MANY_PAY, service.getMerchant());
    }

}
