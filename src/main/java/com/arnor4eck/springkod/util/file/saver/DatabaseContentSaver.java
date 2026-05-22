package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.util.exception.UploadFileException;
import com.arnor4eck.springkod.util.file.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseContentSaver implements ContentSaver {
    private final S3Service s3Service;
    @Override
    public void save(FileSaveClass saveClass) {
        try{
            s3Service.uploadFile(saveClass.key(), saveClass.file());
            log.info("Файл {} загружен в хранилище", saveClass.key());
        }
        catch (Exception ex){
            throw new UploadFileException(
                    String.format("Error on upload file with key %s", saveClass.key()));
        }
    }

    @Override
    public void saveAll(List<FileSaveClass> saveClasses) {
        saveClasses.forEach(this::save);
    }
}
