package com.arnor4eck.springkod.util.file;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public class FileLoadingUtil {

    public static FileImpl getMarkUpFile(List<FileImpl> allFiles) throws FileNotFoundException {
        FileImpl markUpFile = getSoloFileByType(allFiles, FileType.MARKUP_FILE);

        if(markUpFile == null){
            throw new FileNotFoundException("Нет обязательного файла разметки");
        }

        return markUpFile;
    }

    public static Optional<FileImpl> getProbabilityFile(List<FileImpl> allFiles) {
        return Optional.ofNullable(getSoloFileByType(allFiles, FileType.PROBABILITY));
    }

    public static Optional<FileImpl> getMetadataFile(List<FileImpl> allFiles) {
        return Optional.ofNullable(getSoloFileByType(allFiles, FileType.METADATA));
    }

    public static List<FileImpl> getAllImages(List<FileImpl> allFiles) {
        return allFiles.stream()
                .filter(i -> i.getFileType() == FileType.IMAGE)
                .toList();
    }

    public static FileImpl getSoloFileByType(List<FileImpl> allFiles, FileType fileType) {
        for(FileImpl file : allFiles) {
            if(file.getFileType() == fileType)
                return file;
        }

        return null;
    }
}
