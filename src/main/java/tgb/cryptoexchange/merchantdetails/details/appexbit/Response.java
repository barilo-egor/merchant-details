package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Boolean success;

    private List<Offer> addedOffers;

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = new ValidationResult();
        if (Objects.isNull(success)) {
            validationResult.notNull("success");
        } else if (Boolean.FALSE.equals(success)) {
            validationResult.addError("success", "expected true but was false");
        } else {
            if (hasDetails()) {
                if (addedOffers.size() > 1) {
                    validationResult.addError("addedOffers", "size expected <= 1");
                } else {
                    validateOffer(validationResult);
                }
            }
        }
        return validationResult;
    }

    private void validateOffer(ValidationResult validationResult) {
        Offer offer = addedOffers.getFirst();
        if (Objects.isNull(offer)) {
            validationResult.notNull("offer");
        } else {
            if (Objects.isNull(offer.getId())) {
                validationResult.notNull("offer.id");
            }
            if (Objects.isNull(offer.getStatus())) {
                validationResult.notNull("offer.status");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(addedOffers) && !addedOffers.isEmpty();
    }

    @Data
    public static class Offer {

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String id;

        private String message;
    }
}
