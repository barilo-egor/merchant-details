package tgb.cryptoexchange.merchantdetails.details.paybox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.properties.ExtasyPayProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExtasyPayOrderCreationService extends PayBoxOrderCreationService {

    private final ExtasyPayProperties extasyPayProperties;

    protected ExtasyPayOrderCreationService(@Qualifier("extasyPayWebClient") WebClient webClient,
                                            ExtasyPayProperties extasyPayProperties) {
        super(webClient, extasyPayProperties);
        this.extasyPayProperties = extasyPayProperties;
    }

    @Override
    public Merchant getMerchant() {
        return Merchant.EXTASY_PAY;
    }

    @Override
    protected void addHeaders(HttpHeaders httpHeaders, Method method) {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Authorization", "Bearer " + extasyPayProperties.getToken(method));
    }

    @Override
    protected Request body(DetailsRequest detailsRequest) {
        Request request = new Request();
        request.setAmount(detailsRequest.getAmount());
        request.setMerchantTransactionId(UUID.randomUUID().toString());

        Method method = parseMethod(detailsRequest.getCurrentMerchantMethod(), Method.class);
        if (Method.SBER_QR.equals(method) || Method.VTB_QR.equals(method)) {
            request.setBankName(method.getBankName());
        }
        return request;
    }

    @Override
    protected Optional<DetailsResponse> buildResponse(Response response) {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(getMerchant());
        detailsResponse.setMerchantOrderId(response.getId().toString());

        if (Objects.nonNull(response.getPaymentUrl())) {
            detailsResponse.setQr(response.getPaymentUrl());
        } else if (Objects.nonNull(response.getPhoneNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getPhoneNumber());
        } else if (Objects.nonNull(response.getCardNumber())) {
            detailsResponse.setDetails(response.getBankName() + " " + response.getCardNumber());
        } else if (Objects.nonNull(response.getQrImage())) {
            detailsResponse.setQr(response.getQrImage());
        } else {
            detailsResponse.setQr(response.getPaymentLink());
        }

        detailsResponse.setMerchantOrderStatus(Status.PROCESS.name());
        return Optional.of(detailsResponse);
    }
}
