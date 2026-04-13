package tgb.cryptoexchange.merchantdetails.controller;

import com.google.protobuf.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.enums.CryptoCurrency;
import tgb.cryptoexchange.enums.DeliveryType;
import tgb.cryptoexchange.grpc.generated.*;
import tgb.cryptoexchange.merchantdetails.constants.AutoConfirmType;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.dto.MerchantConfigRequest;
import tgb.cryptoexchange.merchantdetails.dto.UpdateMerchantConfigDTO;
import tgb.cryptoexchange.merchantdetails.entity.MerchantConfig;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.service.MerchantConfigService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;
import tgb.cryptoexchange.merchantdetails.service.VariableService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = MerchantDetailsControllerGrpc.class)
@ExtendWith(MockitoExtension.class)
class MerchantDetailsControllerGrpcTest {

    @MockitoBean
    private MerchantDetailsService merchantDetailsService;

    @MockitoBean
    private MerchantConfigService merchantConfigService;

    @MockitoBean
    private VariableService variableService;

    @Autowired
    private MerchantDetailsControllerGrpc grpcController;

    @Autowired
    private MockMvc mockMvc;

    @CsvSource("""
            ALFA_TEAM, f669eb83-a6c2-4456-8416-c2b1fd514c99, CARD
            ONLY_PAYS, 3b8cd3a8-28dc-49a9-a522-91c582c83b5f, SBP
            """)
    @ParameterizedTest
    void cancelShouldCallCancelOrderMethod(Merchant merchant, String orderId, String method) {
        CancelOrderRequestGrpc grpcRequest = CancelOrderRequestGrpc.newBuilder()
                .setMerchant(merchant.name())
                .setOrderId(orderId)
                .setMethod(method)
                .build();
        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);

        grpcController.cancelOrder(grpcRequest, responseObserver);

        ArgumentCaptor<CancelOrderRequest> cancelOrderRequestCaptor = ArgumentCaptor.forClass(CancelOrderRequest.class);
        verify(merchantDetailsService).cancelOrder(eq(merchant), cancelOrderRequestCaptor.capture());

        CancelOrderRequest actual = cancelOrderRequestCaptor.getValue();

        assertAll(
                () -> assertEquals(orderId, actual.getOrderId()),
                () -> assertEquals(method, actual.getMethod()),
                () -> verify(responseObserver).onNext(any(Empty.class)),
                () -> verify(responseObserver).onCompleted()
        );
    }

    @Test
    void getConfigShouldReturnEmptyArrayIfConfigNotFound() {
        Page<MerchantConfigDTO> page = Page.empty();
        when(merchantConfigService.findAll(any(Pageable.class), any())).thenReturn(page);
        MerchantConfigRequestGrpc grpcRequest = MerchantConfigRequestGrpc.newBuilder()
                .setPagination(PaginationGrpc.newBuilder().setPage(0).setSize(20).build())
                .build();
        ArgumentCaptor<MerchantConfigResponseGrpc> responseCaptor =
                ArgumentCaptor.forClass(MerchantConfigResponseGrpc.class);
        StreamObserver<MerchantConfigResponseGrpc> responseObserver = mock(StreamObserver.class);
        grpcController.getConfig(grpcRequest, responseObserver);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        MerchantConfigResponseGrpc actualResponse = responseCaptor.getValue();

        assertAll(
                () -> assertTrue(actualResponse.getSuccess()),
                () -> assertEquals(0, actualResponse.getTotalCount()),
                () -> assertTrue(actualResponse.getDataList().isEmpty()),
                () -> assertEquals(0, actualResponse.getDataCount())
        );
    }

    @Test
    void getConfigShouldReturnConfigArray() {
        List<MerchantConfigDTO> configs = new ArrayList<>();
        configs.add(MerchantConfigDTO.fromEntity(MerchantConfig.builder()
                .id(150L).merchant(Merchant.ALFA_TEAM).isOn(true)
                .minAmount(100).maxAmount(200).merchantOrder(5).isAutoWithdrawalOn(true).build()));
        configs.add(MerchantConfigDTO.fromEntity(MerchantConfig.builder()
                .id(152346L).merchant(Merchant.SETTLE_X).isOn(false)
                .minAmount(150).maxAmount(5000).merchantOrder(2).isAutoWithdrawalOn(true).build()));

        Page<MerchantConfigDTO> page = new PageImpl<>(configs, PageRequest.of(0, 20), 2);
        when(merchantConfigService.findAll(any(Pageable.class), any())).thenReturn(page);

        MerchantConfigRequestGrpc grpcRequest = MerchantConfigRequestGrpc.newBuilder()
                .setPagination(PaginationGrpc.newBuilder().setPage(0).setSize(20).build())
                .build();

        ArgumentCaptor<MerchantConfigResponseGrpc> responseCaptor = ArgumentCaptor.forClass(MerchantConfigResponseGrpc.class);
        StreamObserver<MerchantConfigResponseGrpc> responseObserver = mock(StreamObserver.class);

        grpcController.getConfig(grpcRequest, responseObserver);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        MerchantConfigResponseGrpc actualResponse = responseCaptor.getValue();

        assertAll(
                () -> assertTrue(actualResponse.getSuccess()),
                () -> assertEquals(2, actualResponse.getTotalCount()),
                () -> assertEquals(2, actualResponse.getDataCount()), // Проверка размера списка
                () -> assertEquals(150L, actualResponse.getData(0).getId()),
                () -> assertEquals(Merchant.ALFA_TEAM.name(), actualResponse.getData(0).getMerchant()),
                () -> assertTrue(actualResponse.getData(0).getIsOn()),
                () -> assertEquals(152346L, actualResponse.getData(1).getId()),
                () -> assertEquals(Merchant.SETTLE_X.name(), actualResponse.getData(1).getMerchant()),
                () -> assertFalse(actualResponse.getData(1).getIsOn())
        );
    }

    @Test
    void getConfigShouldCallServiceWithCorrectRequest() {
        Page<MerchantConfigDTO> page = Page.empty();
        when(merchantConfigService.findAll(any(Pageable.class), any())).thenReturn(page);

        MerchantConfigRequestGrpc grpcRequest = MerchantConfigRequestGrpc.newBuilder()
                .setPagination(PaginationGrpc.newBuilder().setPage(0).setSize(20).build())
                .build();

        StreamObserver<MerchantConfigResponseGrpc> responseObserver = mock(StreamObserver.class);

        grpcController.getConfig(grpcRequest, responseObserver);
        ArgumentCaptor<MerchantConfigRequest> requestCaptor = ArgumentCaptor.forClass(MerchantConfigRequest.class);
        verify(merchantConfigService).findAll(any(Pageable.class), requestCaptor.capture());

        MerchantConfigRequest actualRequest = requestCaptor.getValue();
        assertAll(
                () -> assertNotNull(actualRequest),
                () -> verify(responseObserver).onNext(any(MerchantConfigResponseGrpc.class)),
                () -> verify(responseObserver).onCompleted()
        );
    }


    @Test
    void updateConfig_ShouldMapRequestAndCallService() {
        UpdateMerchantConfigRequestGrpc request = UpdateMerchantConfigRequestGrpc.newBuilder()
                .setId(25552)
                .setIsOn(BoolValue.of(true))
                .setUpdateMask(FieldMask.newBuilder().addPaths("is_on").build())
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.updateConfig(request, responseObserver);

        ArgumentCaptor<UpdateMerchantConfigDTO> dtoCaptor = ArgumentCaptor.forClass(UpdateMerchantConfigDTO.class);
        verify(merchantConfigService).update(dtoCaptor.capture());

        UpdateMerchantConfigDTO actual = dtoCaptor.getValue();
        assertAll(
                () -> assertEquals(25552, actual.getId()),
                () -> assertTrue(actual.getIsOn()),
                () -> verify(responseObserver).onNext(any(Empty.class)),
                () -> verify(responseObserver).onCompleted()
        );
    }

    @Test
    void updateConfig_ShouldMapEnumFieldsCorrectly() {
        AutoConfirmConfigDTOGrpc configGrpc = AutoConfirmConfigDTOGrpc.newBuilder()
                .setCryptoCurrency(CryptoCurrency.BITCOIN.name())
                .setAutoConfirmType(AutoConfirmType.AUTO_WITHDRAWAL.name())
                .setDeliveryType(DeliveryType.VIP.name())
                .build();

        UpdateMerchantConfigRequestGrpc request = UpdateMerchantConfigRequestGrpc.newBuilder()
                .setId(1001)
                .addConfirmConfigs(configGrpc)
                .setUpdateMask(FieldMask.newBuilder().addPaths("confirm_configs").build())
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.updateConfig(request, responseObserver);

        ArgumentCaptor<UpdateMerchantConfigDTO> dtoCaptor = ArgumentCaptor.forClass(UpdateMerchantConfigDTO.class);
        verify(merchantConfigService).update(dtoCaptor.capture());

        var actualConfig = dtoCaptor.getValue().getConfirmConfigs().getFirst();
        assertAll(
                () -> assertEquals(1001, dtoCaptor.getValue().getId()),
                () -> assertEquals("BITCOIN", actualConfig.getCryptoCurrency().name()),
                () -> assertEquals("AUTO_WITHDRAWAL", actualConfig.getAutoConfirmType().name()),
                () -> verify(responseObserver).onCompleted()
        );
    }

    @Test
    void updateOrder_ShouldCallChangeOrderWithIsUp() {
        UpdateOrderRequestGrpc request = UpdateOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setIsUp(BoolValue.of(true))
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.updateOrder(request, responseObserver);

        verify(merchantConfigService).changeOrder(Merchant.ALFA_TEAM, true);

        verify(responseObserver).onNext(any(Empty.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void updateOrder_ShouldCallChangeOrderWithNewOrderValue() {
        UpdateOrderRequestGrpc request = UpdateOrderRequestGrpc.newBuilder()
                .setMerchant(Merchant.ALFA_TEAM.name())
                .setNewOrder(Int32Value.of(10))
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.updateOrder(request, responseObserver);

        verify(merchantConfigService).changeOrder(Merchant.ALFA_TEAM, 10);
        verify(responseObserver).onCompleted();
    }

    @Test
    void deleteConfigField_ShouldCallServiceDeleteFieldMethod() {
        DeleteConfigFieldRequestGrpc request = DeleteConfigFieldRequestGrpc.newBuilder()
                .setId(1L)
                .setFieldName("groupChatId")
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.deleteConfigField(request, responseObserver);

        verify(merchantConfigService).deleteField(1L, "groupChatId");
        verify(responseObserver).onNext(any(Empty.class));
        verify(responseObserver).onCompleted();
    }

    @ParameterizedTest
    @EnumSource(VariableType.class)
    void getVariable_ShouldCallServiceAndReturnMappedResponse(VariableType variableType) {
        Variable mockVariable = Variable.builder()
                .id(1L)
                .type(variableType)
                .value(variableType.getDefaultValue())
                .build();

        when(variableService.findByType(variableType)).thenReturn(mockVariable);

        GetVariableRequestGrpc request = GetVariableRequestGrpc.newBuilder()
                .setVariableType(VariableTypeGrpc.valueOf(variableType.name()))
                .build();

        StreamObserver<VariableDTOGrpc> responseObserver = mock(StreamObserver.class);
        grpcController.getVariable(request, responseObserver);

        ArgumentCaptor<VariableDTOGrpc> responseCaptor = ArgumentCaptor.forClass(VariableDTOGrpc.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        VariableDTOGrpc actualResponse = responseCaptor.getValue();
        assertAll(
                () -> assertEquals(variableType.name(), actualResponse.getType().name()),
                () -> assertEquals(variableType.getDefaultValue(), actualResponse.getValue()),
                () -> verify(responseObserver).onCompleted()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ATTEMPTS_COUNT, 8",
            "ATTEMPTS_COUNT, 1",
            "MIN_ATTEMPT_TIME, 25",
            "MIN_ATTEMPT_TIME, 10"
    })
    void updateVariable_ShouldCallServiceUpdateMethod(VariableType variableType, String newValue) {
        UpdateVariableRequestGrpc request = UpdateVariableRequestGrpc.newBuilder()
                .setVariableType(VariableTypeGrpc.valueOf(variableType.name()))
                .setValue(newValue)
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.updateVariable(request, responseObserver);

        verify(variableService).update(variableType, newValue);
        verify(responseObserver).onNext(any(Empty.class));
        verify(responseObserver).onCompleted();
    }

    @ParameterizedTest
    @CsvSource({
            "ALFA_TEAM, 2ba66f30-fd38-4688-b7d1-f8fc592b537a",
            "BIT_ZONE, f7f3ff42-8e0f-4682-9434-4ea3577af076"
    })
    void sendReceipt_ShouldCallServiceWithCorrectData(Merchant merchant, String orderId) {

        byte[] pdfBytes = """
                 %PDF-1.7
                 1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
                 2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
                 3 0 obj <<\s
                   /Type /Page\s
                   /Parent 2 0 R\s
                   /MediaBox [0 0 612 792]\s
                   /Resources << /Font << /F1 4 0 R >> >>\s
                   /Contents 5 0 R\s
                 >> endobj
                 4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
                 5 0 obj << /Length 44 >> stream
                 BT /F1 24 Tf 100 700 Td (Hello, World!) Tj ET
                 endstream
                 xref
                 0 6
                 0000000000 65535 f\s
                 0000000010 00000 n\s
                 0000000059 00000 n\s
                 0000000115 00000 n\s
                 0000000262 00000 n\s
                 0000000332 00000 n\s
                 trailer << /Size 6 /Root 1 0 R >>
                 startxref
                 426
                 %%EOF
                \s""".getBytes(StandardCharsets.UTF_8);
        String fileName = "receipt.pdf";

        SendReceiptRequestGrpc request = SendReceiptRequestGrpc.newBuilder()
                .setMerchant(merchant.name())
                .setOrderId(orderId)
                .setReceiptData(ByteString.copyFrom(pdfBytes))
                .setFileName(fileName)
                .build();

        StreamObserver<Empty> responseObserver = mock(StreamObserver.class);
        grpcController.sendReceipt(request, responseObserver);

        verify(merchantDetailsService).sendReceipt(
                eq(merchant),
                eq(orderId),
                argThat(bytes -> Arrays.equals(pdfBytes, bytes)),
                eq(fileName)
        );
        verify(responseObserver).onNext(any(Empty.class));
        verify(responseObserver).onCompleted();
    }
}