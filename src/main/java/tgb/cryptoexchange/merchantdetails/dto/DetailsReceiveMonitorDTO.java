package tgb.cryptoexchange.merchantdetails.dto;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DetailsReceiveMonitorDTO {

    private Long dealId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean success;

    private List<MerchantAttempt> attempts;

    @Data
    public static class MerchantAttempt {

        private Merchant merchant;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private boolean success;

        private boolean error;
    }
}
