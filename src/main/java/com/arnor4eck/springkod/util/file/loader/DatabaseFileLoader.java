package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.util.file.FileImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.util.List;

@Component
@AllArgsConstructor
public class DatabaseFileLoader implements FileLoader {

    private final ContentLoader contentLoader;

    private final DatasitoryFileRepository datasitoryFileRepository;

    @Override
    public FileImpl load(String key) throws FileNotFoundException {
        if(!datasitoryFileRepository.existsByFileId(key))
            throw new FileNotFoundException("Файл %s не существует.".formatted(key));

        return contentLoader.load(key);
    }

    @Override
    public FileImpl load(long datasitoryId, FileType fileType) throws FileNotFoundException {
        List<DatasitoryFile> files = datasitoryFileRepository.findByDatasitoryIdAndFileType(datasitoryId, fileType);

        if(files.isEmpty())
            throw new FileNotFoundException("Файла датазитория %d типа %s не существует.".formatted(datasitoryId, fileType.name()));
        if(files.size() != 1)
            throw new RuntimeException("Файл не один"); // TODO customize

        return contentLoader.load(files.get(0).getFileId());
    }
}
