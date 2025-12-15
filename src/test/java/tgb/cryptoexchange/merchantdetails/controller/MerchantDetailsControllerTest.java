package tgb.cryptoexchange.merchantdetails.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MerchantDetailsController.class)
@ExtendWith(MockitoExtension.class)
class MerchantDetailsControllerTest {

    @MockitoBean
    private MerchantDetailsService merchantDetailsService;

    @MockitoBean
    private MerchantConfigService merchantConfigService;

    @MockitoBean
    private VariableService variableService;

    @Autowired
    private MockMvc mockMvc;

    @CsvSource("""
            ALFA_TEAM,f669eb83-a6c2-4456-8416-c2b1fd514c99,CARD
            ONLY_PAYS,3b8cd3a8-28dc-49a9-a522-91c582c83b5f,SBP
            """)
    @ParameterizedTest
    void cancelShouldCallCancelOrderMethod(Merchant merchant, String orderId, String method) throws Exception {
        mockMvc.perform(patch("/merchant-details/" + merchant.name())
                .queryParam("orderId", orderId)
                .queryParam("method", method)
        ).andExpect(status().isOk());
        ArgumentCaptor<CancelOrderRequest> cancelOrderRequestCaptor = ArgumentCaptor.forClass(CancelOrderRequest.class);
        verify(merchantDetailsService).cancelOrder(eq(merchant), cancelOrderRequestCaptor.capture());
        CancelOrderRequest actual = cancelOrderRequestCaptor.getValue();
        assertAll(
                () -> assertEquals(orderId, actual.getOrderId()),
                () -> assertEquals(method, actual.getMethod())
        );
    }

    @Test
    void getConfigShouldReturnEmptyArrayIfConfigNotFound() throws Exception {
        Page<MerchantConfigDTO> page = Page.empty();
        when(merchantConfigService.findAll(any(Pageable.class), any())).thenReturn(page);
        mockMvc.perform(get("/merchant-details/config"))
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data").isEmpty());
    }

    @Test
    void getConfigShouldReturnConfigArray() throws Exception {
        List<MerchantConfigDTO> configs = new ArrayList<>();
        configs.add(MerchantConfigDTO.fromEntity(MerchantConfig.builder()
                .id(150L)
                .merchant(Merchant.ALFA_TEAM)
                .isOn(true)
                .minAmount(100)
                .maxAmount(200)
                .merchantOrder(5)
                .isAutoWithdrawalOn(true)
                .build()));
        configs.add(MerchantConfigDTO.fromEntity(MerchantConfig.builder()
                .id(152346L)
                .merchant(Merchant.WELL_BIT)
                .isOn(false)
                .minAmount(150)
                .maxAmount(5000)
                .merchantOrder(2)
                .isAutoWithdrawalOn(true)
                .build()));
        Page<MerchantConfigDTO> page = new PageImpl<>(configs, Mockito.mock(Pageable.class), 2);
        when(merchantConfigService.findAll(any(Pageable.class), any())).thenReturn(page);
        mockMvc.perform(get("/merchant-details/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0]").exists())
                .andExpect(jsonPath("data[1]").exists())
                .andExpect(jsonPath("data[2]").doesNotExist());
    }

    @Test
    void updateConfigShouldCallServiceUpdateMethod() throws Exception {
        mockMvc.perform(patch("/merchant-details/config")
                        .header("Content-Type", "application/json")
                        .content("""
                                {
                                    "id": 25552,
                                    "isOn": true
                                }
                                """))
                .andExpect(status().isOk());
        ArgumentCaptor<UpdateMerchantConfigDTO> dtoCaptor = ArgumentCaptor.forClass(UpdateMerchantConfigDTO.class);
        verify(merchantConfigService).update(dtoCaptor.capture());
        var actual = dtoCaptor.getValue();
        assertAll(
                () -> assertEquals(25552, actual.getId()),
                () -> assertTrue(actual.getIsOn())
        );
    }

    @Test
    void updateOrderShouldPCallServiceChangeOrderMethod() throws Exception {
        mockMvc.perform(patch("/merchant-details/config/order")
                        .param("merchant", "ALFA_TEAM")
                        .param("isUp", "true"))
                .andExpect(status().isOk());
        verify(merchantConfigService).changeOrder(Merchant.ALFA_TEAM, true);
    }

    @Test
    void deleteFieldShouldCallServiceMethod() throws Exception {
        mockMvc.perform(delete("/merchant-details/config/1/groupChatId"))
                .andExpect(status().isOk());
        verify(merchantConfigService).deleteField(1L, "groupChatId");
    }

    @EnumSource(VariableType.class)
    @ParameterizedTest
    void getVariableShouldCallServiceMethod(VariableType variableType) throws Exception {
        when(variableService.findByType(variableType)).thenReturn(Variable.builder().id(1L).type(variableType).value(variableType.getDefaultValue()).build());
        mockMvc.perform(get("/merchant-details/variable/" + variableType.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.type").value(variableType.name()))
                .andExpect(jsonPath("data.value").value(variableType.getDefaultValue()));
    }

    @CsvSource("""
            ATTEMPTS_COUNT,8
            ATTEMPTS_COUNT,1
            MIN_ATTEMPT_TIME,25
            MIN_ATTEMPT_TIME,10
            """)
    @ParameterizedTest
    void updateVariableShouldCallServiceMethod(VariableType variableType, String value) throws Exception {
        mockMvc.perform(patch("/merchant-details/variable/" + variableType.name())
                        .queryParam("value", value))
                .andExpect(status().isOk());
        verify(variableService).update(variableType, value);
    }
}