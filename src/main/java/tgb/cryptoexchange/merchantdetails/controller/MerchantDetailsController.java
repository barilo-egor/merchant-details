package tgb.cryptoexchange.merchantdetails.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

@RestController
@RequestMapping
public class MerchantDetailsController extends ApiController {

    private final MerchantPropertiesService merchantPropertiesService;

    public MerchantDetailsController(MerchantPropertiesService merchantPropertiesService) {
        this.merchantPropertiesService = merchantPropertiesService;
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
    public ResponseEntity<ApiResponse<Object>> requisite(@PathVariable Merchant merchant) {
        Optional<Object> maybeProperties = merchantPropertiesService.getProperties(merchant);
        return maybeProperties
                .map(properties -> new ResponseEntity<>(ApiResponse.success(properties), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
