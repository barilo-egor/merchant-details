package tgb.cryptoexchange.merchantdetails.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;
import tgb.cryptoexchange.web.ApiResponse;

@RestController
@RequestMapping("merchant-requisite")
public class MerchantRequisiteController extends ApiController {

    private final MerchantPropertiesService merchantPropertiesService;

    public MerchantRequisiteController(MerchantPropertiesService merchantPropertiesService) {
        this.merchantPropertiesService = merchantPropertiesService;
    }

    @GetMapping("/properties/{merchant}")
    public ApiResponse<Object> requisite(@PathVariable Merchant merchant) {
        return ApiResponse.success(merchantPropertiesService.getProperties(merchant));
    }
}
