package com.arnor4eck.springkod.util.file.spring;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file.FileImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileSpringLoaderImpl implements FileSpringLoader {
    @Override
    public FileImpl load(MultipartFile file) throws IOException {
        String loadedFileType = file.getContentType();
        FileType fileType = getFileType(loadedFileType);

        return new FileImpl(file, fileType);
    }

    private FileType getFileType(String fileType) {
        if(isImage(fileType)) {
            return FileType.IMAGE;
        } else if(isMarkupFile(fileType)){
            return FileType.MARKUP_FILE;
        }
        throw new IllegalArgumentException("Invalid file type");
    }

    private boolean isImage(String fileType){
        return fileType.startsWith("image");
    }

    private boolean isMarkupFile(String fileType){
        return fileType.equals("text/csv") || fileType.equals("application/json");
    }
}
