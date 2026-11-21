package net.rcetech.md.integrations.platform;

import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

public interface MerchantMethod {

    String name();

    String getDescription();

    Optional<RequestMethod> getRequestMethod();
}
