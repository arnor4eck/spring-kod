package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.util.file.url.CloudinaryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class DatabaseFileSaver implements FileSaver {

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final ContentSaver contentSaver;

    private final CloudinaryService cloudinaryService;

    @Override
    public void save(FileSaveClass saveClass, Datasitory datasitory) {
        DatasitoryFile datasitoryFile = createDatasitoryFile(saveClass, datasitory);

        DatasitoryFile df = datasitoryFileRepository.save(datasitoryFile);
        contentSaver.save(saveClass);
        log.info("Файл {} сохранен в базу данных", df.getFileId());

        if(saveClass.file().getFileType() == FileType.IMAGE) {
            try {
                cloudinaryService.asyncUploadToCloudinary(df, saveClass.file().getBytes());
            } catch (Exception e) {
                log.error("Неудалось загрузить на Cloudinary файл {}", df.getFileId(), e);
            }
        }

        log.info("DatabaseFileSaver отработал.");
    }

    @Override
    public void saveAll(List<FileSaveClass> saveClasses, Datasitory datasitory) {
        saveClasses.forEach(saveClass -> this.save(saveClass, datasitory));

        contentSaver.saveAll(saveClasses);
    }

    private DatasitoryFile createDatasitoryFile(FileSaveClass saveClass,
                                                Datasitory datasitory) {
        return DatasitoryFile.builder()
                .datasitory(datasitory)
                .fileId(saveClass.key())
                .fileType(saveClass.file().getFileType())
                .build();
    }
}
