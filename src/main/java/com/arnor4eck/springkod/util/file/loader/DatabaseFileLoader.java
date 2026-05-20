package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.util.file.FileImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

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
}
