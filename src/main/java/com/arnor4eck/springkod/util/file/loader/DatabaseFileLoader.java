package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.util.exception.FileNotFoundInStorageException;
import com.arnor4eck.springkod.util.file.FileImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class DatabaseFileLoader implements FileLoader {

    private final ContentLoader contentLoader;

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final DatasitoryRepository datasitoryRepository; // TODO change query

    @Override
    public FileImpl load(String key) throws FileNotFoundInStorageException {
        Optional<DatasitoryFile> datasitoryFile = datasitoryFileRepository.findByFileId(key);

        if(datasitoryFile.isEmpty())
            throw new FileNotFoundInStorageException("Файл %s не существует.".formatted(key));
        if(datasitoryFile.get().getFileType() == FileType.UNKNOWN_IMAGE)
            throw new FileNotFoundInStorageException("Файл %s имеет некорректный формат".formatted(key));
        log.info("Файл {} найден в базе данных", key);

        return contentLoader.load(key);
    }

    @Override
    public FileImpl load(long datasitoryId, FileType fileType) throws FileNotFoundInStorageException {
        if(fileType == FileType.UNKNOWN_IMAGE)
            throw new IllegalArgumentException("Невозможно выгрузить некорретный файл");

        List<DatasitoryFile> files = datasitoryFileRepository.findByDatasitoryIdAndFileType(datasitoryId, fileType);

        if(files.isEmpty())
            throw new FileNotFoundInStorageException("Файла датазитория %d типа %s не существует.".formatted(datasitoryId, fileType.name()));
        if(files.size() != 1)
            throw new FileNotFoundInStorageException("Файл не один");

        log.info("Файл {} для датазитория {} найден в базе данных", fileType.name(), datasitoryId);

        return contentLoader.load(files.getFirst().getFileId());
    }

    @Override
    public List<FileImpl> loadAll(long datasitoryId) throws FileNotFoundInStorageException {
        List<DatasitoryFile> files =
                datasitoryFileRepository.findAllByDatasitory(datasitoryRepository.findById(datasitoryId).get())
                .stream()
                .filter(df -> df.getFileType() != FileType.UNKNOWN_IMAGE)
                .toList();

        List<FileImpl> result = new LinkedList<>();

        for(DatasitoryFile datasitoryFile : files) {
            result.add(contentLoader.load(datasitoryFile.getFileId()));
        }

        return result;
    }
}
