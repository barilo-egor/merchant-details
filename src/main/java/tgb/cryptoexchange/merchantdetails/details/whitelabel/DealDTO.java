package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class DealDTO {

    private String id;

    @JsonDeserialize(using = Bank.Deserializer.class)
    private Bank paymentMethod;

    private RequisitesDTO requisites;
}
