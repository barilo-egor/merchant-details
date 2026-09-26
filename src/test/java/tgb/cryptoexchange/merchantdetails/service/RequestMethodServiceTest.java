package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.MerchantConstants;
import tgb.cryptoexchange.merchantdetails.details.bridgepay.Method;
import tgb.cryptoexchange.merchantdetails.detailsapi.enums.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RequestMethodServiceTest {

    private static final RequestMethodService methodService = new RequestMethodService();

    @BeforeAll
    static void setUp() {
        methodService.init();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ALFA_TEAM", "EVO_PAY"
    })
    void getMerchantMethods_shouldReturnEmptyListIfPassedEmptyRequestMethods(Merchant merchant) {
        assertEquals(0, methodService.getMerchantMethods(merchant, new ArrayList<>()).size());
    }

    @Test
    void getMerchantMethods_shouldReturnAlfaCardMethod() {
        List<String> merchantMethods = methodService.getMerchantMethods(Merchant.ALFA_TEAM, List.of(RequestMethod.CARD));

        assertEquals(1, merchantMethods.size());
        assertEquals(Method.TO_CARD.name(), merchantMethods.getFirst());
    }

    @Test
    void getMerchantMethods_shouldReturnAlfaCardAndSbpMethods() {
        List<String> merchantMethods = methodService.getMerchantMethods(
                Merchant.ALFA_TEAM,
                List.of(RequestMethod.CARD, RequestMethod.SBP)
        );

        assertEquals(2, merchantMethods.size());
        assertAll(
                () -> assertTrue(merchantMethods.contains(Method.TO_CARD.name())),
                () -> assertTrue(merchantMethods.contains(Method.SBP.name()))
        );
    }

    @ParameterizedTest
    @EnumSource(RequestMethod.class)
    void getMerchants_shouldReturnMerchantsWithRequestedMethod(RequestMethod requestMethod) {
        Set<Merchant> merchants = methodService.getMerchants(List.of(requestMethod));
        for (Merchant merchant : merchants) {
            assertTrue(Arrays.stream(MerchantConstants.valueOf(merchant.name()).getMethods())
                    .anyMatch(method -> method.getRequestMethod().isPresent()
                            && method.getRequestMethod().get().equals(requestMethod)));
        }
    }

}