package tgb.cryptoexchange.merchantdetails.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.exception.DeleteReceiptException;
import tgb.cryptoexchange.merchantdetails.exception.SaveReceiptException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ReceiptService {

    private final String baseUrl;

    public static final String RECEIPT_FOLDER = "receipts";

    public ReceiptService(@Value("${gateway-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String saveReceipt(byte[] fileContent, String fileName, String folderName) {
        try {
            String jarPath = System.getProperty("user.dir");
            Path directoryPath = Paths.get(jarPath, RECEIPT_FOLDER, folderName);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            File targetFile = directoryPath.resolve(fileName).toFile();
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(fileContent);
            }
            return generateExternalUrl(fileName, folderName);
        } catch (IOException e) {
            String errorMsg = "Ошибка при попытке сохранить чек " + fileName;
            log.error(errorMsg, e);
            throw new SaveReceiptException(errorMsg, e);
        }
    }

    public void deleteReceipt(String fileName, String folderName) {
        try {
            String jarPath = System.getProperty("user.dir");
            Path filePath = Paths.get(jarPath, RECEIPT_FOLDER, folderName).resolve(fileName);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("Чек {} успешно удален из папки {}", fileName, folderName);
            } else {
                log.warn("Не удалось удалить чек {}: файл не найден в папке {}", fileName, folderName);
            }
        } catch (IOException e) {
            String errorMsg = String.format("Ошибка при попытке удалить чек %s из папки %s", fileName, folderName);
            log.error(errorMsg, e);
            throw new DeleteReceiptException(errorMsg, e);
        }
    }

    private String generateExternalUrl(String fileName, String folderName) {
        return baseUrl + "/" + RECEIPT_FOLDER + "/" + folderName + "/" + fileName;
    }

}
