package net.rcetech.md.integrations.platform;

public interface MerchantDetailsResponse {

    ValidationResult validate();

    boolean hasDetails();
}
