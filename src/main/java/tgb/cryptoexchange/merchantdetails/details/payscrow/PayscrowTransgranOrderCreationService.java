package tgb.cryptoexchange.merchantdetails.details.payscrow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.properties.PayscrowTransgranProperties;

import java.net.URI;
import java.util.function.Function;

@Service
public class PayscrowTransgranOrderCreationService extends PayscrowOrderCreationService {

    protected PayscrowTransgranOrderCreationService(@Qualifier("payscrowTransgranWebClient") WebClient webClient,
                                                    PayscrowTransgranProperties payscrowTransgranProperties) {
        super(webClient, payscrowTransgranProperties);
    }

    @Override
    protected Function<UriBuilder, URI> uriBuilder(DetailsRequest detailsRequest) {
        return uriBuilder -> uriBuilder.path("/api/v1/form/create").build();
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.PAYSCROW_TRANSGRAN;
    }
}
