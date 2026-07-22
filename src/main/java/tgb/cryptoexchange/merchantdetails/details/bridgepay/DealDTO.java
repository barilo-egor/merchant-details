package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DealDTO {

    private String paymentMethod;

    private String paymentMethodName;

    @JsonDeserialize(using = Method.Deserializer.class)
    private Method paymentOption;

    private RequisitesDTO requisites;

    private String qrCodeLink;

    public String getPaymentMethod() {
        Bank bankValue = Bank.fromValue(paymentMethod);
        if (Objects.nonNull(bankValue)) {
            return bankValue.getDisplayName();
        }
        return Objects.nonNull(paymentMethodName) ? paymentMethodName : paymentMethod;
    }
}
