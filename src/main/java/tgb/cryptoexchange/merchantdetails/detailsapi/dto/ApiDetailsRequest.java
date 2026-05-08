package tgb.cryptoexchange.merchantdetails.detailsapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Transient;
import lombok.Data;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.MerchantConstants;
import tgb.cryptoexchange.merchantdetails.details.MerchantMethod;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDetailsRequest {

    private String requestId;

    private String internalId;

    private String userId;

    private Integer amount;

    private List<RequestMethod> requestMethods = new ArrayList<>();

    @Transient
    public List<String> getMerchantMethods(Merchant merchant) {
        List<String> merchantMethodNames = MerchantConstants.getMethods(merchant).stream().map(MerchantMethod::name).toList();
        return this.requestMethods.stream().map(Enum::name).filter(merchantMethodNames::contains).toList();
    }

}
