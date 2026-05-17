package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.util.exception.DownloadFileException;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseContentLoader implements ContentLoader{ // TODO Саша после реализации своей мега-задумки
    private final S3Service s3Service;
    @Override
    public FileImpl load(String key) {
        FileImpl saveFile;
        try {
            saveFile = s3Service.downloadFile(key);
        }
        catch (Exception ex){
            throw new DownloadFileException(
                    String.format("Error on download file with key %s", key));
        }
        return saveFile;
    }
}
