package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.GambitSimProperties;

import java.util.Optional;

@Service
@Slf4j
public class GambitSimOrderCreationService extends GambitOrderCreationService {

    protected GambitSimOrderCreationService(@Qualifier("gambitSimWebClient") WebClient gambitSimWebClient,
                                            GambitSimProperties gambitSimProperties) {
        super(gambitSimWebClient, gambitSimProperties);
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(response.getAmount().intValue());
        Response.Requisites requisites = response.getPaymentDetails();
        detailsResponse.setOperator(requisites.getOperator());
        detailsResponse.setDetails(requisites.getPhone());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.GAMBIT_SIM;
    }

}
