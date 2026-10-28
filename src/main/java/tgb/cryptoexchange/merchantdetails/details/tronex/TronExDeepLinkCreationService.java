package tgb.cryptoexchange.merchantdetails.details.tronex;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.config.CallbackConfig;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.TronExDLProperties;

import java.util.Optional;

@Service
public class TronExDeepLinkCreationService extends TronExOrderCreationService {


    protected TronExDeepLinkCreationService(@Qualifier("tronExWebClient") WebClient webClient,
                                            TronExDLProperties tronExDlProperties, CallbackConfig callbackConfig) {
        super(webClient, tronExDlProperties, callbackConfig);
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        Response.Data responseData = response.getPlatform();
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(responseData.getId());
        detailsResponse.setAmount(Integer.valueOf(responseData.getAmount()));
        // Не возвращает статус в ответе
        detailsResponse.setMerchantOrderStatus(Status.CREATED.name());

        detailsResponse.setQr(tronExProperties.dlUrl() + "/" + responseData.getId());

        return Optional.of(detailsResponse);
    }


    @Override
    public Merchant getMerchant() {
        return Merchant.TRON_EX_DEEP_LINK;
    }

}
