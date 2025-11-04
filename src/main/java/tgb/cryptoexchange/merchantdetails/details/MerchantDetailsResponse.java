package tgb.cryptoexchange.merchantdetails.details;

public interface MerchantDetailsResponse {

    ValidationResult validate();

    boolean hasDetails();
}
