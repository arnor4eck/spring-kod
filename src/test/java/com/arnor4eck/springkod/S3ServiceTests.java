package com.arnor4eck.springkod;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file.FileImpl; // Скорректируй импорт под свой пакет

import com.arnor4eck.springkod.util.file.S3Service; // Скорректируй импорт под свой пакет
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTests {

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private AmazonS3 s3Client;

    private final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    public void setUp() {
        // Прогреваем приватное поле @Value через рефлексию Spring
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }
    // Этот метод должен находиться внутри класса S3ServiceTests
    private FileImpl createMockFile() {
        // Берем первый доступный элемент из твоего Enum FileType (например, IMAGE)
        FileType sampleType = FileType.values()[0];

        return new FileImpl(
                "user-1.png",
                "original_avatar.png",
                "image/png",
                sampleType,
                "fake-image-bytes".getBytes()
        );
    }

    @Test
    public void testUploadFileSuccess() throws IOException {
        String key = "avatars/user-1.png";
        FileImpl file = createMockFile();

        // Нам не нужно мокать putObject детально, так как он возвращает PutObjectResult,
        // который в коде сервиса никак не используется (метод возвращает void).
        // Просто проверяем, что метод вызвался без ошибок.
        s3Service.uploadFile(key, file);

        // Проверяем, что клиент действительно вызывался с правильными параметрами
        verify(s3Client, times(1)).putObject(eq(BUCKET_NAME), eq(key), any(), any());
    }

    @Test
    public void testDownloadFileSuccess() throws IOException {
        String key = "documents/resume.pdf";
        byte[] expectedContent = "Hello Selectel S3".getBytes();

        // Готовим фейковые метаданные, которые якобы вернул S3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");

        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("original-name", "my_resume.pdf");
        userMetadata.put("file-type", "DOCUMENT"); // Замени DOCUMENT на свой элемент Enum
        metadata.setUserMetadata(userMetadata);

        // Собираем S3Object для возврата из мока
        S3Object s3Object = new S3Object();
        s3Object.setObjectMetadata(metadata);

        // Оборачиваем наши байты в специфичный для AWS стрим S3ObjectInputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(expectedContent);
        S3ObjectInputStream s3InputStream = new S3ObjectInputStream(bais, new HttpGet());
        s3Object.setObjectContent(s3InputStream);

        // Описываем поведение мока
        when(s3Client.getObject(BUCKET_NAME, key)).thenReturn(s3Object);

        // Тестируем метод
        FileImpl resultFile = s3Service.downloadFile(key);

        // Проверки (Assertions)
        Assertions.assertNotNull(resultFile);
        Assertions.assertEquals("resume.pdf", resultFile.getName());
        Assertions.assertEquals("my_resume.pdf", resultFile.getOriginalFilename());
        Assertions.assertEquals("application/pdf", resultFile.getContentType());
        Assertions.assertArrayEquals(expectedContent, resultFile.getBytes());
    }

    @Test
    public void testDownloadFileThrowsExceptionWhenS3Fails() {
        String key = "broken/file.txt";

        // Имитируем падение самого клиента S3 (например, файла нет или сеть легла)
        when(s3Client.getObject(BUCKET_NAME, key)).thenThrow(new RuntimeException("S3 Error"));

        // Проверяем, что метод пробрасывает ошибку наверх
        Assertions.assertThrows(RuntimeException.class, () -> s3Service.downloadFile(key));
    }

//    @Test
//    public void testDeleteFileSuccess() {
//        String key = "trash/old_photo.jpg";
//
//        s3Service.deleteFile(key);
//
//        // Проверяем, что метод удаления на клиенте был вызван ровно один раз
//        verify(s3Client, times(1)).deleteObject(BUCKET_NAME, key);
//    }
//
//    // Хелпер для создания тестового FileImpl
//    private FileImpl createMockFile() {
//        // Подставь сюда валидный элемент своего Enum FileType (например, FileType.IMAGE)
//        FileType sampleType = FileType.values()[0];
//
//        return new FileImpl(
//                "user-1.png",
//                "original_avatar.png",
//                "image/png",
//                sampleType,
//                "fake-image-bytes".getBytes()
//        );
//      }
}