package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.util.exception.UploadFileException;
import com.arnor4eck.springkod.util.file.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseContentSaver implements ContentSaver {
    private final S3Service s3Service;
    @Override
    public void save(FileSaveClass saveClass) {
        try{
            s3Service.uploadFile(saveClass.key(), saveClass.file());
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
