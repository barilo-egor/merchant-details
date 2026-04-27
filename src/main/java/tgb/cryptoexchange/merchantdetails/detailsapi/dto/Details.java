package tgb.cryptoexchange.merchantdetails.detailsapi.dto;

import lombok.Builder;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

@Data
@Builder
public class Details {

    private RequestMethod requestMethod;

    private String details;

    private String bank;

    private String operator;

}
