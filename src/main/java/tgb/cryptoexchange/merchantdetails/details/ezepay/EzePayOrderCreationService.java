package tgb.cryptoexchange.merchantdetails.details.ezepay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.EzePayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
public class EzePayOrderCreationService extends MerchantOrderCreationService<Response> {
    
    private final EzePayProperties ezePayProperties;
    
    protected EzePayOrderCreationService(@Qualifier("ezePayWebClient") WebClient webClient, EzePayProperties ezePayProperties) {
        super(webClient, Response.class);
        this.ezePayProperties = ezePayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EZE_PAY;
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/createOrder/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(DetailsRequest detailsRequest, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setOrder(UUID.randomUUID().toString());
        request.setShopId(Long.parseLong(ezePayProperties.id()));
        request.setKey(ezePayProperties.key());
        request.setAmount(detailsRequest.getAmount());
        request.setBank(parseMethod(detailsRequest.getMethod(), Method.class).getId());
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse requisiteVO = new DetailsResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(response.getData().getOrderId());
        requisiteVO.setMerchantOrderStatus(Status.CHOOSING_METHOD.name());
        String requisite;
        if (Objects.nonNull(response.getData().getBank())) {
            requisite = response.getData().getBank() + " " + response.getData().getDetails();
        } else {
            requisite = response.getData().getBankSbp() + " " + response.getData().getDetails();
        }
        requisiteVO.setDetails(requisite);
        return Optional.of(requisiteVO);
    }
}
