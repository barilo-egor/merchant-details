package tgb.cryptoexchange.merchantdetails.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptServiceTest {

    private final String baseUrl = "http://localhost:8080";
    private ReceiptService receiptService;
    private String originalUserDir;
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        receiptService = new ReceiptService(baseUrl);
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void saveReceipt_Success_ShouldCreateFileAndReturnCorrectUrl() throws IOException {
        byte[] content = "test content".getBytes();
        String fileName = "receipt_123.pdf";
        String folderName = "orders";
        String expectedUrl = baseUrl + "/" + ReceiptService.RECEIPT_FOLDER + "/" + folderName + "/" + fileName;

        String actualUrl = receiptService.saveReceipt(content, fileName, folderName);

        assertEquals(expectedUrl, actualUrl);

        Path expectedFilePath = tempDir.resolve(ReceiptService.RECEIPT_FOLDER).resolve(folderName).resolve(fileName);
        assertTrue(Files.exists(expectedFilePath));
        assertArrayEquals(content, Files.readAllBytes(expectedFilePath));
    }

    @Test
    void deleteReceipt_FileExists_ShouldDeleteFileSuccessfully() throws IOException {
        String fileName = "delete_me.pdf";
        String folderName = "refunds";

        Path fileFolder = tempDir.resolve(ReceiptService.RECEIPT_FOLDER).resolve(folderName);
        Files.createDirectories(fileFolder);
        Path targetFile = fileFolder.resolve(fileName);
        Files.createFile(targetFile);

        assertTrue(Files.exists(targetFile), "Файл должен существовать перед удалением");

        assertDoesNotThrow(() -> receiptService.deleteReceipt(fileName, folderName));

        assertFalse(Files.exists(targetFile), "Файл должен быть удален");
    }

    @Test
    void deleteReceipt_FileDoesNotExist_ShouldNotThrowException() {
        String fileName = "non_existent.pdf";
        String folderName = "unknown_folder";

        assertDoesNotThrow(() -> receiptService.deleteReceipt(fileName, folderName));
    }

}
