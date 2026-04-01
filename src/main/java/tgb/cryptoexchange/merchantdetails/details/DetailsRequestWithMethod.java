package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequestWithMethod extends DetailsRequest {

    private final String currentMerchantMethod;

    public DetailsRequestWithMethod(DetailsRequest detailsRequest, String merchantMethod) {
        this.requestId = detailsRequest.getRequestId();
        this.currentMerchantMethod = merchantMethod;
        this.id = detailsRequest.getId();
        this.amount = detailsRequest.getAmount();
        this.chatId = detailsRequest.getChatId();
        this.initiatorApp = detailsRequest.getInitiatorApp();
        this.methods = detailsRequest.getMethods();
    }

    @Override
    public String getCurrentMerchantMethod() {
        return currentMerchantMethod;
    }

}
