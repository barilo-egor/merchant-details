package net.rcetech.md.integrations.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ежедневная чистка чеков, оставшихся на диске: по ордеру не пришёл финальный callback
 * или удаление по callback'у не отработало. Удаляются чеки старше суток,
 * чтобы не тронуть чек ордера, по которому сделка ещё идёт.
 */
@Service
@Slf4j
public class ReceiptCleanupService {

    private static final Duration MAX_RECEIPT_AGE = Duration.ofHours(24);

    /**
     * Чистит папки с чеками раз в сутки в 03:00.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        Path receiptsFolder = Paths.get(System.getProperty("user.dir"), ReceiptService.RECEIPT_FOLDER);
        if (!Files.exists(receiptsFolder) || !Files.isDirectory(receiptsFolder)) {
            log.debug("Папка {} не существует, чистка чеков не требуется", receiptsFolder);
            return;
        }
        FileTime oldestAllowed = FileTime.from(Instant.now().minus(MAX_RECEIPT_AGE));
        int deletedCount = 0;
        try (Stream<Path> files = Files.walk(receiptsFolder)) {
            List<Path> receiptFiles = files.filter(Files::isRegularFile).toList();
            for (Path receiptFile : receiptFiles) {
                try {
                    if (Files.getLastModifiedTime(receiptFile).compareTo(oldestAllowed) < 0) {
                        Files.delete(receiptFile);
                        deletedCount++;
                        log.debug("Чек {} удален при чистке", receiptFile);
                    }
                } catch (IOException e) {
                    log.error("Ошибка при попытке удалить чек {} при чистке", receiptFile, e);
                }
            }
        } catch (IOException e) {
            log.error("Ошибка при обходе папки с чеками {}", receiptsFolder, e);
            return;
        }
        log.info("Чистка чеков завершена, удалено чеков старше суток: {}", deletedCount);
    }

}
