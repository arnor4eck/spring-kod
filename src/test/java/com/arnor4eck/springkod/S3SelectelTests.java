package com.arnor4eck.springkod;


import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.S3Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.UUID;

@SpringBootTest // Поднимает весь контекст приложения
@ActiveProfiles("test") // Ищет именно application-test.yml (или .properties)
public class S3SelectelTests {

    @Autowired
    private S3Service s3Service;

    @Test
    public void testSelectelS3Lifecycle() throws IOException {
        // Генерируем случайный ключ для файла, чтобы тесты не конфликтовали
        String key = "tests/integration-file-" + UUID.randomUUID() + ".txt";
        byte[] content = "Hello Selectel Real Bucket Test".getBytes();

        FileImpl fileToUpload = new FileImpl(
                "test.txt",
                "original_test.txt",
                "text/plain",
                FileType.values()[0], // берем дефолтный тип из твоего Enum
                content
        );

        try {
            // 1. Проверяем загрузку в облако Selectel
            Assertions.assertDoesNotThrow(() -> s3Service.uploadFile(key, fileToUpload));

            // 2. Скачиваем обратно и сверяем данные
            FileImpl downloadedFile = s3Service.downloadFile(key);

            Assertions.assertNotNull(downloadedFile);
            Assertions.assertArrayEquals(content, downloadedFile.getBytes());
            Assertions.assertEquals("original_test.txt", downloadedFile.getOriginalFilename());

        } finally {
            // 3. Блок зачистки: удаляем за собой файл из облака в любом случае
            try {
                s3Service.deleteFile(key);
            } catch (Exception e) {
                System.err.println("Не удалось удалить тестовый файл: " + e.getMessage());
            }
        }
    }
}
