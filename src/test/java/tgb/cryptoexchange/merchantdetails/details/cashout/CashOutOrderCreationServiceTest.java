package tgb.cryptoexchange.merchantdetails.details.cashout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.CashOutProperties;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashOutOrderCreationServiceTest {

    @Mock
    private CashOutProperties cashOutProperties;

    @InjectMocks
    private CashOutOrderCreationService service;

    @Test
    void getMerchantShouldReturnCashOutMerchant() {
        assertEquals(Merchant.CASH_OUT, service.getMerchant());
    }


    @Test
    void uriBuilder_ShouldBuildCorrectUri() {
        UriBuilder builder = UriComponentsBuilder.newInstance();
        Function<UriBuilder, URI> uriFunction = service.uriBuilder(mock(DetailsRequest.class));

        URI result = uriFunction.apply(builder);

        assertThat(result.getPath()).isEqualTo("/merchants/transactions/create-manual");
    }

    @Test
    void headers_ShouldSetRequiredHeaders() {
        when(cashOutProperties.key()).thenReturn("cashout-key");
        HttpHeaders headers = new HttpHeaders();

        Consumer<HttpHeaders> headersConsumer = service.headers(null, null);
        headersConsumer.accept(headers);

        assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
        assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer cashout-key");
    }

    @Test
    void buildResponse_ShouldMapFieldsCorrectly() {
        Response response = new Response();
        Response.ResponseRequisite data = new Response.ResponseRequisite();
        data.setTransactionId("TX-1");
        data.setStatus(Status.PENDING);
        data.setAmount("500");

        Response.PaymentDetails details = new Response.PaymentDetails();
        details.setBankName("Sber");
        details.setCardNumber("11112222");
        data.setPaymentDetails(details);
        response.setData(data);

        Optional<DetailsResponse> result = service.buildResponse(response);

        assertThat(result).isPresent();
        DetailsResponse dr = result.get();
        assertThat(dr.getMerchantOrderId()).isEqualTo("TX-1");
        assertThat(dr.getDetails()).isEqualTo("Sber 11112222");
        assertThat(dr.getAmount()).isEqualTo(500);
    }
}
