package tgb.cryptoexchange.merchantdetails.service;

import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.controller.MerchantDetailsController;

import java.util.Arrays;
import java.util.List;

@Service
public class MerchantApiService {

    private static final Semver VERSION_0_9_1 = new Semver(MerchantDetailsController.VERSION_0_9_1);

    private static final List<Merchant> MERCHANTS_VERSION_0_9_1 = Arrays.stream(Merchant.values())
            .filter(merchant -> !Merchant.PLATA_PAYMENT.equals(merchant))
            .toList();

    public List<Merchant> getMerchantsByApiVersion(String apiVersion) {
        Semver semver;
        try {
            semver = new Semver(apiVersion);
        } catch (SemverException e) {
            semver = VERSION_0_9_1;
        }
        if (semver.isGreaterThanOrEqualTo("0.10.0")) {
            return Arrays.asList(Merchant.values());
        } else {
            return MERCHANTS_VERSION_0_9_1;
        }
    }
}
