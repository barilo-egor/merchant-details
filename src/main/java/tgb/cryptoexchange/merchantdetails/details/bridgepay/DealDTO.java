package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class DealDTO {
    @JsonDeserialize(using = Bank.Deserializer.class)
    private Bank paymentMethod;

    private RequisitesDTO requisites;
}
