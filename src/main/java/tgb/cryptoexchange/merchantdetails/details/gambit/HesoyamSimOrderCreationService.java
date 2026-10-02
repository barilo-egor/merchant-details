package tgb.cryptoexchange.merchantdetails.details.gambit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.HesoyamSimProperties;

import java.util.Optional;

@Service
@Slf4j
public class HesoyamSimOrderCreationService extends GambitOrderCreationService {

    protected HesoyamSimOrderCreationService(@Qualifier("hesoyamWebClient") WebClient hesoyamWebClient,
                                             HesoyamSimProperties hesoyamSimProperties) {
        super(hesoyamWebClient, hesoyamSimProperties);
    }

    protected HesoyamMethod parseDetailsMethod(String method) {
        return parseMethod(method, HesoyamMethod.class);
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchantOrderId(response.getId());
        detailsResponse.setMerchantOrderStatus(response.getStatus().name());
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setAmount(response.getAmount().intValue());
        Response.Requisites requisites = response.getPaymentDetails();
        detailsResponse.setDetails(requisites.getOperator() + " " + requisites.getPhone());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.HESOYAM_SIM;
    }

}
