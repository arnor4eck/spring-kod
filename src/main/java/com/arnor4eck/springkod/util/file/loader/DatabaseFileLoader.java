package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.util.file.FileImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class DatabaseFileLoader implements FileLoader {

    private final ContentLoader contentLoader;

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final DatasitoryRepository datasitoryRepository; // TODO change query

    @Override
    public FileImpl load(String key) throws FileNotFoundException {
        if(!datasitoryFileRepository.existsByFileId(key))
            throw new FileNotFoundException("Файл %s не существует.".formatted(key));
        log.info("Файл {} найден в базе данных", key);

        return contentLoader.load(key);
    }

    @Override
    public FileImpl load(long datasitoryId, FileType fileType) throws FileNotFoundException {
        List<DatasitoryFile> files = datasitoryFileRepository.findByDatasitoryIdAndFileType(datasitoryId, fileType);

        if(files.isEmpty())
            throw new FileNotFoundException("Файла датазитория %d типа %s не существует.".formatted(datasitoryId, fileType.name()));
        if(files.size() != 1)
            throw new FileNotFoundException("Файл не один");

        log.info("Файл {} для датазитория {} найден в базе данных", fileType.name(), datasitoryId);

        return contentLoader.load(files.getFirst().getFileId());
    }

    @Override
    public List<FileImpl> loadAll(long datasitoryId) throws FileNotFoundException {
        List<DatasitoryFile> files = datasitoryFileRepository.findAllByDatasitory(datasitoryRepository.findById(datasitoryId).get());

        List<FileImpl> result = new LinkedList<>();

        for(DatasitoryFile datasitoryFile : files) {
            result.add(contentLoader.load(datasitoryFile.getFileId()));
        }

        return result;
    }
}
