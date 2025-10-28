package tgb.cryptoexchange.merchantdetails.details.whitelabel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class DealDTO {

    private String id;

    @JsonDeserialize(using = PaymentMethod.Deserializer.class)
    private PaymentMethod paymentMethod;

    private RequisitesDTO requisites;
}
