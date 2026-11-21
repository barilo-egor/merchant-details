package net.rcetech.md.app.detailsapi;

import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.app.grpc.generated.DetailsRequestGrpc;
import net.rcetech.md.app.grpc.generated.DetailsResponseGrpc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ожидания processing от gRPC-ответа мд с реквизитами.
 */
public final class ProcessingContract {

    public static final List<String> PROCESSING_METHODS = List.of("CARD", "SBP");

    private ProcessingContract() {
    }

    /**
     * Запрос реквизитов в том виде, в котором его отправляет processing.
     *
     * @param userId  идентификатор пользователя клиента, {@code null}, если клиент его не передал
     * @param methods методы оплаты из запроса клиента
     * @return gRPC-запрос
     */
    public static DetailsRequestGrpc processingRequest(String userId, String... methods) {
        DetailsRequestGrpc.Builder builder = DetailsRequestGrpc.newBuilder()
                .setInternalId(UUID.randomUUID().toString())
                .setAmount(2000)
                .setWaitTimeout(29)
                .addAllRequestMethod(List.of(methods))
                .setOwnerId(UUID.randomUUID().toString())
                .setInitiatorApp("processing");
        if (userId != null) {
            builder.setUserId(userId);
        }
        return builder.build();
    }

    /**
     * Проверяет, что processing сможет разобрать ответ и выдать клиенту реквизиты.
     *
     * @param response         ответ мд
     * @param requestedMethods методы из запроса
     */
    public static void assertSatisfies(DetailsResponseGrpc response, List<String> requestedMethods) {
        assertAll(
                () -> assertDoesNotThrow(() -> Merchant.valueOf(response.getMerchant())),
                () -> assertFalse(response.getOrderId().isBlank()),
                () -> assertTrue(response.hasDetails()),
                () -> assertTrue(PROCESSING_METHODS.contains(response.getDetails().getRequestMethod())),
                () -> assertTrue(requestedMethods.contains(response.getDetails().getRequestMethod())),
                () -> assertFalse(response.getDetails().getDetails().isBlank()),
                () -> assertTrue(response.getDetails().hasBank()),
                () -> assertFalse(response.getDetails().getBank().isBlank()),
                () -> assertTrue(!response.hasAmount() || response.getAmount() > 0)
        );
    }
}
