package tgb.cryptoexchange.merchantdetails.kafka;

import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("!kafka-disabled")
public class DetailsRequestTopicListener {

    private static final String VERSION_0_9_1 = "0.9.1";

    private static final Set<Merchant> MERCHANTS_VERSION_0_9_1 = Arrays.stream(Merchant.values())
            .filter(merchant -> !Merchant.PLATA_PAYMENT.equals(merchant))
            .collect(Collectors.toSet());

    private final DetailsRequestProcessorService detailsRequestProcessorService;

    public DetailsRequestTopicListener(DetailsRequestProcessorService detailsRequestProcessorService) {
        this.detailsRequestProcessorService = detailsRequestProcessorService;
    }

    @KafkaListener(topics = "${kafka.topic.merchant-details.request}", groupId = "${kafka.group-id}")
    public void receive(@Payload DetailsRequest request, @Header(name = "API-version", defaultValue = VERSION_0_9_1) String version) {
        Collection<Merchant> merchants;
        Semver semver;
        try {
            semver = new Semver(version);
        } catch (SemverException e) {
            semver = new Semver(VERSION_0_9_1);
        }
        if (semver.isGreaterThanOrEqualTo("0.10.0")) {
            merchants = Arrays.asList(Merchant.values());
        } else {
            merchants = MERCHANTS_VERSION_0_9_1;
        }
        detailsRequestProcessorService.process(request, merchants);
    }
}
