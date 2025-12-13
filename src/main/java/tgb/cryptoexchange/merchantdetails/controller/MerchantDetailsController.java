package tgb.cryptoexchange.merchantdetails.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigResponse;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

@RestController
@RequestMapping("/merchant-details")
public class MerchantDetailsController extends ApiController {

    private final MerchantPropertiesService merchantPropertiesService;

    private final MerchantDetailsService merchantDetailsService;

    private final MerchantConfigService merchantConfigService;

    public MerchantDetailsController(MerchantPropertiesService merchantPropertiesService,
                                     MerchantDetailsService merchantDetailsService,
                                     MerchantConfigService merchantConfigService) {
        this.merchantPropertiesService = merchantPropertiesService;
        this.merchantDetailsService = merchantDetailsService;
        this.merchantConfigService = merchantConfigService;
    }

    @Operation(summary = "Получение списка пропертей мерчанта.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Проперти мерчанта найдены."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Проперти для данного мерчанта не найдены"
            )
    })
    @GetMapping("/properties/{merchant}")
    public ResponseEntity<ApiResponse<Object>> properties(@PathVariable Merchant merchant) {
        Optional<Object> maybeProperties = merchantPropertiesService.getProperties(merchant);
        return maybeProperties
                .map(properties -> new ResponseEntity<>(ApiResponse.success(properties), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Попытка получения реквизитов у запрошенного мерчанта.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Реквизиты мерчанта найдены."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", description = "Реквизиты у мерчанта получены не были."
            )
    })
    @GetMapping("/{merchant}")
    public ResponseEntity<ApiResponse<DetailsResponse>> details(@Valid @ModelAttribute DetailsRequest request,
                                                                  @PathVariable Merchant merchant) {
        Optional<DetailsResponse> maybeRequisiteResponse = merchantDetailsService.getDetails(merchant, request);
        return maybeRequisiteResponse
                .map(requisiteResponse -> new ResponseEntity<>(ApiResponse.success(requisiteResponse), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DetailsResponse>> details(@Valid @ModelAttribute DetailsRequest request) {
        Optional<DetailsResponse> maybeRequisiteResponse = merchantDetailsService.getDetails(request);
        return maybeRequisiteResponse
                .map(requisiteResponse -> new ResponseEntity<>(ApiResponse.success(requisiteResponse), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PatchMapping("/{merchant}")
    @ResponseStatus(HttpStatus.OK)
    public void cancel(@PathVariable Merchant merchant, @ModelAttribute CancelOrderRequest cancelOrderRequest) {
        merchantDetailsService.cancelOrder(merchant, cancelOrderRequest);
    }

    @GetMapping("/config")
    public ResponseEntity<MerchantConfigResponse> getConfig(@PageableDefault(size = 20) Pageable pageable,
                                                         @ModelAttribute MerchantConfigRequest merchantConfigRequest) {
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
}
