package tgb.cryptoexchange.merchantdetails.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.dto.*;
import tgb.cryptoexchange.merchantdetails.service.MerchantApiService;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;
import tgb.cryptoexchange.web.ApiResponse;

@RestController
@RequestMapping("/merchant-details")
public class MerchantDetailsController extends ApiController {

    public static final String VERSION_0_9_1 = "0.9.1";

    private final MerchantDetailsService merchantDetailsService;

    private final MerchantConfigService merchantConfigService;

    private final VariableService variableService;
    
    private final MerchantApiService merchantApiService;

    public MerchantDetailsController(MerchantDetailsService merchantDetailsService,
                                     MerchantConfigService merchantConfigService, VariableService variableService,
                                     MerchantApiService merchantApiService) {
        this.merchantDetailsService = merchantDetailsService;
        this.merchantConfigService = merchantConfigService;
        this.variableService = variableService;
        this.merchantApiService = merchantApiService;
    }

    @PatchMapping("/{merchant}")
    @ResponseStatus(HttpStatus.OK)
    public void cancel(@PathVariable Merchant merchant, @ModelAttribute CancelOrderRequest cancelOrderRequest) {
        merchantDetailsService.cancelOrder(merchant, cancelOrderRequest);
    }

    @GetMapping("/config")
    public ResponseEntity<MerchantConfigResponse> getConfig(@PageableDefault(size = 20) Pageable pageable,
                                                            @ModelAttribute MerchantConfigRequest merchantConfigRequest,
                                                            @RequestHeader(name = "API-Version", defaultValue = "0.9.1") String version) {
        merchantConfigRequest.setMerchants(merchantApiService.getMerchantsByApiVersion(version));
        Page<MerchantConfigDTO> page = merchantConfigService.findAll(pageable, merchantConfigRequest);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(new MerchantConfigResponse(page.getContent()));
    }

    @PatchMapping("/config")
    @ResponseStatus(HttpStatus.OK)
    public void updateConfig(@RequestBody UpdateMerchantConfigDTO dto) {
        merchantConfigService.update(dto);
    }

    @DeleteMapping("/config/{id}/{fieldName}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteConfigField(@PathVariable Long id, @PathVariable String fieldName) {
        merchantConfigService.deleteField(id, fieldName);
    }

    @PatchMapping("/config/order")
    @ResponseStatus(HttpStatus.OK)
    public void updateOrder(@RequestParam Merchant merchant, @RequestParam Boolean isUp) {
        merchantConfigService.changeOrder(merchant, isUp);
    }

    @GetMapping("/variable/{variableType}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<VariableDTO>> getVariable(@PathVariable VariableType variableType) {
        return new ResponseEntity<>(ApiResponse.success(
                VariableDTO.fromEntity(variableService.findByType(variableType))),
                HttpStatus.OK
        );
    }

    @PatchMapping("/variable/{variableType}")
    @ResponseStatus(HttpStatus.OK)
    public void updateVariable(@PathVariable VariableType variableType, @RequestParam String value) {
        variableService.update(variableType, value);
    }

    @PostMapping("/receipt/{merchant}/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public void receipt(@PathVariable Merchant merchant, @PathVariable String orderId,
                        @RequestParam MultipartFile receipt) {
        merchantDetailsService.sendReceipt(merchant, orderId, receipt);
    }
}
