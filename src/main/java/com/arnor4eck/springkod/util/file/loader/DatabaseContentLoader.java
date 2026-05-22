package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.util.exception.DownloadFileException;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseContentLoader implements ContentLoader {

    private final S3Service s3Service;

    @Override
    public FileImpl load(String key) {
        try {
            FileImpl saveFile = s3Service.downloadFile(key);
            log.info("Файл {} найден в хранилище", key);
            return saveFile;
        }
        catch (Exception ex){
            throw new DownloadFileException(
                    String.format("Error on download file with key %s", key));
        }
    }
}
