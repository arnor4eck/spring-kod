package com.arnor4eck.springkod.util.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.exception.DeleteFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class S3Service {


    private final AmazonS3 s3Client;

    @Value("${selectel.s3.bucket}")
    private String bucketName;

    public void uploadFile(String key, FileImpl file) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // Сохраняем кастомные поля FileImpl в User Metadata
        Map<String, String> userMetadata = new HashMap<>();
        if (file.getOriginalFilename() != null) {
            userMetadata.put("original-name", file.getOriginalFilename());
        }
        if (file.getFileType() != null) {
            // Предполагаем, что FileType — это Enum. Сохраняем его как String.
            userMetadata.put("file-type", file.getFileType().name());
        }
        metadata.setUserMetadata(userMetadata);

        s3Client.putObject(bucketName, key, file.getInputStream(), metadata);
    }

    /**
     * НОВЫЙ МЕТОД СКАЧИВАНИЯ
     * Возвращает полностью собранный FileImpl
     */
    public FileImpl downloadFile(String key) throws IOException {
        // 1. Получаем объект из Selectel S3
        S3Object s3Object = s3Client.getObject(bucketName, key);
        ObjectMetadata metadata = s3Object.getObjectMetadata();

        // 2. Вычитываем контент в массив байт
        byte[] content;
        try (var inputStream = s3Object.getObjectContent()) {
            content = IOUtils.toByteArray(inputStream);
        }

        // 3. Извлекаем стандартные метаданные
        String contentType = metadata.getContentType();

        // В качестве системного имени ('name') можно взять сам ключ
        // или очистить его от папок (например, "images/avatar.png" -> "avatar.png")
        String name = key.contains("/") ? key.substring(key.lastIndexOf("/") + 1) : key;

        // 4. Извлекаем наши кастомные метаданные
        // Важно: S3 приводит ключи пользовательских метаданных к нижнему регистру!
        Map<String, String> userMetadata = metadata.getUserMetadata();
        String originalName = userMetadata.get("original-name");
        String fileTypeStr = userMetadata.get("file-type");

        // Восстанавливаем FileType (если это Enum)
        FileType fileType = null;
        if (fileTypeStr != null) {
            try {
                fileType = FileType.valueOf(fileTypeStr);
            } catch (IllegalArgumentException e) {
                // Обработка на случай, если тип не распознан (или дефолтное значение)
                fileType = null;
            }
        }

        // 5. Возвращаем FileImpl через конструктор
        return new FileImpl(name, originalName, contentType, fileType, content);
    }
    public void deleteFile(String key){
        try{
            s3Client.deleteObject(bucketName, key);
        }
        catch (Exception ex){
            throw new DeleteFileException(
                    String.format("Error delete file with key %s", key));
        }
    }
}