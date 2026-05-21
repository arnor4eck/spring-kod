package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;
import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
import com.arnor4eck.springkod.repository.DatasitoryFileRepository;
import com.arnor4eck.springkod.repository.DatasitoryRepository;
import com.arnor4eck.springkod.repository.ImageUrlRepository;
import com.arnor4eck.springkod.util.exception.DatasitoryNotFoundException;
import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.loader.FileLoader;
import com.arnor4eck.springkod.util.file.saver.FileSaveClass;
import com.arnor4eck.springkod.util.file.saver.FileSaver;
import com.arnor4eck.springkod.util.file.spring.FileSpringLoader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class FileService {

    private final FileSpringLoader fileSpringLoader;

    private final FileSaver fileSaver;

    private final FileLoader fileLoader;

    private final DatasitoryRepository datasitoryRepository;

    private final DatasitoryFileRepository datasitoryFileRepository;

    private final ImageUrlRepository imageUrlRepository;

    public void saveImages(List<MultipartFile> files, long datasitoryId){
        // TODO проверка на битые файлы

        List<FileImpl> mapped = mapAllFiles(files);
        List<FileSaveClass> saveClasses = mapped.stream()
                                .map(fi ->
                                    new FileSaveClass(generateKey(fi, datasitoryId), fi))
                                .toList();
        Datasitory datasitory = findDatasitoryById(datasitoryId);

        fileSaver.saveAll(saveClasses, datasitory);
    }

    private List<FileImpl> mapAllFiles(List<MultipartFile> files) {
        try {
            List<FileImpl> mapped = new LinkedList<>();

            for (MultipartFile file : files) {
                mapped.add(fileSpringLoader.load(file));
            }
            return mapped;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String generateKey(FileImpl file, long datasitoryId){
        return String.format("%d-%s", datasitoryId, file.getOriginalFilename());
    }

    public List<ImageUrl> loadImages(long datasitoryId) {
        //Datasitory datasitory = findDatasitoryById(datasitoryId);

        /*// TODO ТОЛЬКО  ФОТО, ЩАС ПОДГРУЖАЕТСЯ ВСЁ
        List<DatasitoryFile> files = datasitoryFileRepository.findAllByDatasitory(datasitory);

        List<FileImpl> found = new LinkedList<>();
        for(DatasitoryFile file : files) {
            try{
                found.add(fileLoader.load(file.getFileId()));
            }catch(FileNotFoundException e){
                log.warn("Файл {} не найден, пропуск", file.getFileId());
            }
        }*/

        return imageUrlRepository.findAllByDatasitoryId(datasitoryId);
    }

    private Datasitory findDatasitoryById(long datasitoryId) {
        return datasitoryRepository.findById(datasitoryId)
                .orElseThrow(() -> new DatasitoryNotFoundException(
                        "Датазитоия с id %d нет".formatted(datasitoryId)));
    }

    public FileImpl loadImage(String name) {
        try{
            return fileLoader.load(name);
        }catch(FileNotFoundException e){
            log.warn("Файл {} не найден, пропуск", name);
            throw new RuntimeException(e);
        }
    }
}
