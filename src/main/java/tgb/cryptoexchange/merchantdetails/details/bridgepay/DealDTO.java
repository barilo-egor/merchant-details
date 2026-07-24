package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DealDTO {

    @JsonDeserialize(using = Bank.Deserializer.class)
    private Bank paymentMethod;

    @JsonDeserialize(using = Method.Deserializer.class)
    private Method paymentOption;

    private RequisitesDTO requisites;

    private String qrCodeLink;
}
