package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.repository.ImageUrlRepository;
import com.arnor4eck.springkod.util.file.url.ImageUrlSaver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class DatabaseFileSaver implements FileSaver {

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final ImageUrlRepository imageUrlsRepository;

    private final ImageUrlSaver imageUrlSaver;

    private final ContentSaver contentSaver;

    @Override
    public void save(FileSaveClass saveClass, Datasitory datasitory) {
        DatasitoryFile datasitoryFile = createDatasitoryFile(saveClass, datasitory);

        DatasitoryFile df = datasitoryFileRepository.save(datasitoryFile);
        log.info("Файл {} сохранен в базу данных", df.getFileId());
        // --
        if(saveClass.file().getFileType() == FileType.IMAGE) {
            try {
                String url = imageUrlSaver.upload(saveClass.file().getBytes());
                imageUrlsRepository.save(ImageUrl.builder()
                        .datasitoryFile(df)
                        .url(url)
                        .build());
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }
        // --
        contentSaver.save(saveClass);
    }

    @Override
    public void saveAll(List<FileSaveClass> saveClasses, Datasitory datasitory) {
        /*datasitoryFileRepository.saveAll(
            saveClasses.stream()
                    .map(sc -> createDatasitoryFile(sc, datasitory))
                    .toList()
        );*/

        // TODO REFACTOR
        saveClasses.forEach(saveClass -> this.save(saveClass, datasitory));

        contentSaver.saveAll(saveClasses);
    }

    private record Temp(String url, DatasitoryFile datasitoryFile) {}

    private DatasitoryFile createDatasitoryFile(FileSaveClass saveClass,
                                                Datasitory datasitory) {
        return DatasitoryFile.builder()
                .datasitory(datasitory)
                .fileId(saveClass.key())
                .fileType(saveClass.file().getFileType())
                .build();
    }
}
