package tgb.cryptoexchange.merchantdetails.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.details.DetailsRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.MerchantServiceRegistry;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

@RestController
@RequestMapping
public class MerchantDetailsController extends ApiController {

    private final MerchantPropertiesService merchantPropertiesService;

    private final MerchantServiceRegistry merchantServiceRegistry;

    public MerchantDetailsController(MerchantPropertiesService merchantPropertiesService,
                                     MerchantServiceRegistry merchantServiceRegistry) {
        this.merchantPropertiesService = merchantPropertiesService;
        this.merchantServiceRegistry = merchantServiceRegistry;
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Мерчант неактивен."
            )
    })
    @GetMapping("/{merchant}")
    public ResponseEntity<ApiResponse<DetailsResponse>> details(@Valid @ModelAttribute DetailsRequest request,
                                                                  @PathVariable Merchant merchant) {
        var maybeCreationService = merchantServiceRegistry.getService(merchant);
        if (maybeCreationService.isPresent()) {
            Optional<DetailsResponse> maybeRequisiteResponse = maybeCreationService.get().createOrder(request);
            return maybeRequisiteResponse
                    .map(requisiteResponse -> new ResponseEntity<>(ApiResponse.success(requisiteResponse), HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
        }
        return new ResponseEntity<>(
                ApiResponse.error("Creation for this merchant is not implemented."),
                HttpStatus.BAD_REQUEST
        );
    }
}
