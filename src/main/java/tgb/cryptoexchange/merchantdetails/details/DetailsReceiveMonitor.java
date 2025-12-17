package tgb.cryptoexchange.merchantdetails.details;

import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.dto.DetailsReceiveMonitorDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetailsReceiveMonitor {

    private final Long dealId;

    private final Integer amount;
    
    private final LocalDateTime startTime;
    
    private LocalDateTime endTime;

    private boolean success;

    private final List<MerchantAttempt> attempts = new ArrayList<>();

    public DetailsReceiveMonitor(Long dealId, Integer amount) {
        this.dealId = dealId;
        this.amount = amount;
        this.startTime = LocalDateTime.now();
    }

    public MerchantAttempt start(Merchant merchant, String method) {
        if (Objects.nonNull(endTime)) {
            throw new UnsupportedOperationException("An attempt cannot be added because the monitor is closed.");
        }
        MerchantAttempt merchantAttempt = new MerchantAttempt();
        merchantAttempt.merchant = merchant;
        merchantAttempt.method = method;
        merchantAttempt.startTime = LocalDateTime.now();
        attempts.add(merchantAttempt);
        return merchantAttempt;
    }

    public void stop(boolean success) {
        this.success = success;
        this.endTime = LocalDateTime.now();
    }

    public DetailsReceiveMonitorDTO toDTO() {
        DetailsReceiveMonitorDTO dto = new DetailsReceiveMonitorDTO();
        dto.setDealId(dealId);
        dto.setAmount(amount);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setSuccess(success);
        List<DetailsReceiveMonitorDTO.MerchantAttempt> dtoAttempts = new ArrayList<>();
        for (MerchantAttempt merchantAttempt : attempts) {
            DetailsReceiveMonitorDTO.MerchantAttempt dtoAttempt = new DetailsReceiveMonitorDTO.MerchantAttempt();
            dtoAttempt.setMerchant(merchantAttempt.merchant);
            dtoAttempt.setMethod(merchantAttempt.method);
            dtoAttempt.setStartTime(merchantAttempt.startTime);
            dtoAttempt.setEndTime(merchantAttempt.endTime);
            dtoAttempt.setSuccess(merchantAttempt.success);
            dtoAttempt.setError(merchantAttempt.error);
            dtoAttempts.add(dtoAttempt);
        }
        dto.setAttempts(dtoAttempts);
        return dto;
    }

    public static class MerchantAttempt {

        private Merchant merchant;

        private String method;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private boolean success;

        private boolean error;

        public void stop(boolean success) {
            this.success = success;
            this.error = false;
            this.endTime = LocalDateTime.now();
        }

        public void error() {
            this.success = false;
            this.error = true;
            this.endTime = LocalDateTime.now();
        }
    }
}
