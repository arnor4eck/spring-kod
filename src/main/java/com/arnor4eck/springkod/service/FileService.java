package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;
import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
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

    private final ImageUrlRepository imageUrlRepository;

    public void saveImages(List<MultipartFile> files, long datasitoryId){
        // TODO проверка на битые файлы ОНИ СОХРАНЯЮТСЯ В БД НА УРОВНЕ ENUM ???

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

    private FileImpl map(MultipartFile file) {
        try{
            return fileSpringLoader.load(file);
        }catch (IOException e){
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String generateKey(FileImpl file, long datasitoryId){
        return String.format("%d-%s", datasitoryId, file.getOriginalFilename());
    }

    public List<ImageUrl> loadImages(long datasitoryId) {
        return imageUrlRepository.findAllByDatasitoryId(datasitoryId);
    }

    private Datasitory findDatasitoryById(long datasitoryId) {
        return datasitoryRepository.findById(datasitoryId)
                .orElseThrow(() -> new DatasitoryNotFoundException(
                        "Датазитоия с id %d нет".formatted(datasitoryId)));
    }

    public FileImpl loadFile(String name) {
        try{
            return fileLoader.load(name);
        }catch(FileNotFoundException e){
            log.warn("Файл {} не найден, пропуск", name);
            throw new RuntimeException(e);
        }
    }

    public FileImpl loadFile(long datasitoryId, FileType fileType) {
        try{
            return fileLoader.load(datasitoryId, fileType);
        }catch(FileNotFoundException e){
            log.warn("Файл в датазитори с id {} не найден, пропуск", datasitoryId);
            throw new RuntimeException(e);
        }
    }

    private void saveProbability(MultipartFile file, long datasitoryId) {
        saveFile(file, datasitoryId);
    }

    public void saveFile(MultipartFile file, long datasitoryId,
                         FileType fileType) {
        switch (fileType) {
            case MARKUP_FILE:
                saveMarkup(file, datasitoryId);
                break;
            case PROBABILITY:
                saveProbability(file, datasitoryId);
                break;
            case METADATA:
                saveMetadata(file, datasitoryId);
                break;
            default: throw new UnsupportedOperationException("Не поддерживается ");
        }
    }

    private void saveMetadata(MultipartFile file, long datasitoryId) {
        FileImpl fileImpl = map(file);
        fileImpl.setFileType(FileType.METADATA); // TODO ПЕРЕДЕЛАТЬ

        Datasitory datasitory = findDatasitoryById(datasitoryId);
        fileSaver.save(new FileSaveClass(generateKey(fileImpl, datasitoryId), fileImpl), datasitory);
    }

    private void saveMarkup(MultipartFile file, long datasitoryId) {
        saveFile(file, datasitoryId);
    }

    private void saveFile(MultipartFile file, long datasitoryId){
        FileImpl fileImpl = map(file);

        Datasitory datasitory = findDatasitoryById(datasitoryId);
        fileSaver.save(new FileSaveClass(generateKey(fileImpl, datasitoryId), fileImpl), datasitory);
    }
}
