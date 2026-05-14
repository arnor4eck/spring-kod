package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DatabaseFileSaver implements FileSaver {

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final ContentSaver contentSaver;

    @Override
    public void save(FileSaveClass saveClass, Datasitory datasitory) {
        DatasitoryFile datasitoryFile = createDatasitoryFile(saveClass, datasitory);

        datasitoryFileRepository.save(datasitoryFile);
        contentSaver.save(saveClass);
    }

    @Override
    public void saveAll(List<FileSaveClass> saveClasses, Datasitory datasitory) {
        datasitoryFileRepository.saveAll(
            saveClasses.stream()
                    .map(sc -> createDatasitoryFile(sc, datasitory))
                    .toList()
        );

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
