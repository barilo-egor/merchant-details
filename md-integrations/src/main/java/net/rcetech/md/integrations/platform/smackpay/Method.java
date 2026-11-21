package net.rcetech.md.integrations.platform.smackpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rcetech.md.integrations.platform.MerchantMethod;
import net.rcetech.md.meta.detailsapi.enums.RequestMethod;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Method implements MerchantMethod {

    QR("QR", "", null),
    NSPK_QR("NSPK QR", "/qr-kyc", null);

    private final String description;

    private final String url;

    final RequestMethod requestMethod;

    public Optional<RequestMethod> getRequestMethod() {
        return Optional.ofNullable(requestMethod);
    }

}
