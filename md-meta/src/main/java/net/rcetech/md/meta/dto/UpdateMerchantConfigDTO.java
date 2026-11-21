package net.rcetech.md.meta.dto;

import lombok.Builder;
import net.rcetech.md.meta.enums.RequiredReceipt;

import java.util.List;

/**
 * Изменения конфига мерчанта: обновляются только поля, отличные от {@code null}.
 */
@Builder
public record UpdateMerchantConfigDTO(
        Long id,
        Boolean isOn,
        Boolean isAutoWithdrawalOn,
        List<String> successStatuses,
        Integer maxAmount,
        Integer minAmount,
        Long groupChatId,
        Integer minDealsCount,
        RequiredReceipt requiredReceipt,
        List<AutoConfirmConfigDTO> confirmConfigs
) {
}
