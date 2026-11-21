package net.rcetech.md.integrations.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptCleanupServiceTest {

    private final ReceiptCleanupService receiptCleanupService = new ReceiptCleanupService();

    private String originalUserDir;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void cleanup_ShouldDeleteReceiptsOlderThanDayAndKeepFreshOnes() throws IOException {
        Path watPdfFolder = tempDir.resolve(ReceiptService.RECEIPT_FOLDER).resolve("wat_pdf");
        Files.createDirectories(watPdfFolder);
        Path staleReceiptFile = watPdfFolder.resolve("order-1.pdf");
        Files.createFile(staleReceiptFile);
        Files.setLastModifiedTime(staleReceiptFile, FileTime.from(Instant.now().minus(Duration.ofHours(25))));
        Path freshReceiptFile = watPdfFolder.resolve("order-2.pdf");
        Files.createFile(freshReceiptFile);

        receiptCleanupService.cleanup();

        assertFalse(Files.exists(staleReceiptFile), "Чек старше суток должен быть удален");
        assertTrue(Files.exists(freshReceiptFile), "Свежий чек должен остаться");
        assertTrue(Files.exists(watPdfFolder), "Папка чеков должна остаться");
    }

    @Test
    void cleanup_ShouldDeleteOldReceiptsFromAllFolders() throws IOException {
        Path watPdfFolder = tempDir.resolve(ReceiptService.RECEIPT_FOLDER).resolve("wat_pdf");
        Path watFolder = tempDir.resolve(ReceiptService.RECEIPT_FOLDER).resolve("wat");
        Files.createDirectories(watPdfFolder);
        Files.createDirectories(watFolder);
        Path watPdfReceiptFile = watPdfFolder.resolve("order-1.pdf");
        Files.createFile(watPdfReceiptFile);
        Path watReceiptFile = watFolder.resolve("order-2.png");
        Files.createFile(watReceiptFile);
        FileTime twoDaysAgo = FileTime.from(Instant.now().minus(Duration.ofHours(49)));
        Files.setLastModifiedTime(watPdfReceiptFile, twoDaysAgo);
        Files.setLastModifiedTime(watReceiptFile, twoDaysAgo);

        receiptCleanupService.cleanup();

        assertFalse(Files.exists(watPdfReceiptFile), "Чек должен быть удален");
        assertFalse(Files.exists(watReceiptFile), "Чек должен быть удален");
        assertTrue(Files.exists(watPdfFolder), "Папка чеков должна остаться");
        assertTrue(Files.exists(watFolder), "Папка чеков должна остаться");
    }

    @Test
    void cleanup_EmptyReceiptsFolder_ShouldNotThrowException() throws IOException {
        Files.createDirectories(tempDir.resolve(ReceiptService.RECEIPT_FOLDER));

        assertDoesNotThrow(receiptCleanupService::cleanup);
    }

    @Test
    void cleanup_MissingReceiptsFolder_ShouldNotThrowException() {
        assertDoesNotThrow(receiptCleanupService::cleanup);
    }

}
