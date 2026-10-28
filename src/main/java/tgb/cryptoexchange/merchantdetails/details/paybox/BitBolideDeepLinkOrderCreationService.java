package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.BitBolideDeepLinkProperties;

import java.util.Objects;
import java.util.Optional;

@Service
public class BitBolideDeepLinkOrderCreationService extends PayBoxOrderCreationService {

    private final BitBolideDeepLinkProperties bitBolideProperties;

    protected BitBolideDeepLinkOrderCreationService(@Qualifier("bitBolideWebClient") WebClient webClient,
                                                    BitBolideDeepLinkProperties bitBolideProperties) {
        super(webClient, bitBolideProperties);
        this.bitBolideProperties = bitBolideProperties;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + bitBolideProperties.token());
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());
        if (Objects.nonNull(response.getPaymentLink())) {
            detailsResponse.setQr(response.getPaymentLink());
        } else if (Objects.nonNull(response.getPaymentUrl())) {
            detailsResponse.setQr(response.getPaymentUrl());
        }
        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.BIT_BOLIDE_DEEP_LINK;
    }

}
