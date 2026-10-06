package tgb.cryptoexchange.merchantdetails.detailsapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDetailsRequest implements DetailsRequest {

    private String requestId;

    private String internalId;

    private String userId;

    private Integer amount;

    private Integer waitTimeout = 60;

    private List<RequestMethod> requestMethods = new ArrayList<>();

    private UUID ownerId;

    @Override
    public String getId() {
        return getInternalId();
    }

    @Override
    public String getInitiatorApp() {
        return "processing";
    }

}
