package net.rcetech.md.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.integrations.platform.CancelOrderRequest;
import net.rcetech.md.app.dto.*;
import net.rcetech.md.meta.dto.*;
import net.rcetech.md.meta.enums.ConfigType;
import net.rcetech.md.app.service.MerchantConfigService;
import net.rcetech.md.integrations.platform.PlatformService;
import net.rcetech.md.app.service.VariableService;

import java.util.List;

import static net.rcetech.md.meta.enums.ConfigType.DEFAULT_VALUE;

@RestController
@RequestMapping("/merchant-details")
public class MerchantDetailsController {

    private final PlatformService platformService;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;

    public MerchantDetailsController(PlatformService platformService,
                                     MerchantConfigService merchantConfigService, VariableService variableService) {
        this.platformService = platformService;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
    }

    @PatchMapping("/{merchant}")
    @ResponseStatus(HttpStatus.OK)
    public void cancel(@PathVariable Merchant merchant, @ModelAttribute CancelOrderRequest cancelOrderRequest) {
        platformService.cancelOrder(merchant, cancelOrderRequest);
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<MerchantConfigDTO>>> getConfig(@PageableDefault(size = 20) Pageable pageable,
                                                                         @ModelAttribute MerchantConfigRequest merchantConfigRequest) {
        Page<MerchantConfigDTO> page = merchantConfigService.findAll(pageable, merchantConfigRequest);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(ApiResponse.success(page.getContent()));
    }

    @PatchMapping("/config")
    @ResponseStatus(HttpStatus.OK)
    public void updateConfig(@RequestBody UpdateMerchantConfigDTO dto) {
        merchantConfigService.update(dto);
    }

    @GetMapping("/config/all-group")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<Long>>> getGroupChatIds() {
        return new ResponseEntity<>(ApiResponse.success(
                merchantConfigService.findAllGroupChatIds()),
                HttpStatus.OK);
    }

    @DeleteMapping("/config/{id}/{fieldName}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteConfigField(@PathVariable Long id, @PathVariable String fieldName) {
        merchantConfigService.deleteField(id, fieldName);
    }

    @PatchMapping("/config/order")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(@RequestParam Merchant merchantFirst, @RequestParam(required = false) Merchant merchantSecond,
                            @RequestParam(required = false) Integer newOrder) {
        if (merchantSecond != null) {
            merchantConfigService.changeOrder(merchantFirst, merchantSecond);
        } else if (newOrder != null) {
            merchantConfigService.changeOrder(merchantFirst, newOrder);
        }
    }

    @PatchMapping("/config/min-deals-count")
    @ResponseStatus(HttpStatus.OK)
    public void updateMinDealsCount(@RequestParam Merchant merchant, @RequestParam Integer newCount) {
        if (newCount < 0) {
            return;
        }
        merchantConfigService.changeMinDealsCount(merchant, newCount);
    }

    @GetMapping("/variable/{variableType}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<VariableDTO>> getVariable(@PathVariable VariableType variableType,
                                                                @RequestParam(defaultValue = DEFAULT_VALUE) ConfigType configType) {
        return new ResponseEntity<>(ApiResponse.success(
                VariableDTO.fromEntity(variableService.findByTypeAndConfigType(variableType, configType))),
                HttpStatus.OK
        );
    }

    @PatchMapping("/variable/{variableType}")
    @ResponseStatus(HttpStatus.OK)
    public void updateVariable(@PathVariable VariableType variableType,
                               @RequestParam String value, @RequestParam(defaultValue = DEFAULT_VALUE) ConfigType configType) {
        variableService.update(variableType, configType, value);
    }

    @PostMapping("/receipt/{merchant}/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void receipt(@PathVariable Merchant merchant, @PathVariable String orderId,
                        @RequestParam MultipartFile receipt) {
        platformService.sendReceipt(merchant, orderId, receipt);
    }

}
