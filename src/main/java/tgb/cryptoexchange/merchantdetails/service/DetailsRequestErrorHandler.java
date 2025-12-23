package tgb.cryptoexchange.merchantdetails.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import java.util.Objects;

@Slf4j
@Component
public class DetailsRequestErrorHandler implements ErrorHandler {

    @Override
    public void handleError(@Nullable Throwable t) {
        if (Objects.nonNull(t)) {
            log.error("Ошибка при обработке DetailsRequest: {}", t.getMessage(), t);
        }
    }
}
