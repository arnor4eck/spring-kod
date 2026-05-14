package com.arnor4eck.springkod.util.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Преобразование MultipartFile в FileImpl
 * */
public interface FileSpringLoader {
    FileImpl load(MultipartFile file) throws IOException;
}
