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
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDetailsRequest {

    private String requestId;

    private String internalId;

    private String userId;

    private Integer amount;

    private Integer waitTimeout = 60;

    private List<RequestMethod> requestMethods = new ArrayList<>();

    private UUID ownerId;

}
