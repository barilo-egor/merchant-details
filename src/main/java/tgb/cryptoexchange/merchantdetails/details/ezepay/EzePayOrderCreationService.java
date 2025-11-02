package tgb.cryptoexchange.merchantdetails.details.ezepay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.merchantdetails.details.MerchantOrderCreationService;
import tgb.cryptoexchange.merchantdetails.details.RequisiteRequest;
import tgb.cryptoexchange.merchantdetails.details.RequisiteResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.EzePayProperties;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
    protected Function<UriBuilder, URI> uriBuilder(RequisiteRequest requisiteRequest) {
        return uriBuilder -> uriBuilder.path("/createOrder/").build();
    }

    @Override
    protected Consumer<HttpHeaders> headers(RequisiteRequest requisiteRequest, String body) {
        return httpHeaders -> httpHeaders.add("Content-Type", "application/json");
    }

    @Override
    protected Object body(RequisiteRequest requisiteRequest) {
        Request request = new Request();
        request.setOrder(UUID.randomUUID().toString());
        request.setShopId(Long.parseLong(ezePayProperties.id()));
        request.setKey(ezePayProperties.key());
        request.setAmount(requisiteRequest.getAmount());
        request.setBank(parseMethod(requisiteRequest.getMethod(), Method.class).getId());
        return request;
    }

    @Override
    protected Optional<RequisiteResponse> buildResponse(Response response) {
        if (!response.getStatus().equals("success")
                || Objects.isNull(response.getData())
                || (Objects.isNull(response.getData().getBank()) && Objects.isNull(response.getData().getBankSbp()))
                || Objects.isNull(response.getData().getDetails())
                || Objects.isNull(response.getData().getOrderId())) {
            return Optional.empty();
        }
        RequisiteResponse requisiteVO = getRequisiteResponse(response);
        return Optional.of(requisiteVO);
    }

    private RequisiteResponse getRequisiteResponse(Response response) {
        RequisiteResponse requisiteVO = new RequisiteResponse();
        requisiteVO.setMerchant(getMerchant());
        requisiteVO.setMerchantOrderId(response.getData().getOrderId());
        requisiteVO.setMerchantOrderStatus(Status.CHOOSING_METHOD.name());
        String requisite;
        if (Objects.nonNull(response.getData().getBank())) {
            requisite = response.getData().getBank() + " " + response.getData().getDetails();
        } else {
            requisite = response.getData().getBankSbp() + " " + response.getData().getDetails();
        }
        requisiteVO.setRequisite(requisite);
        return requisiteVO;
    }
}
