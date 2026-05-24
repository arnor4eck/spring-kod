package com.arnor4eck.springkod.service;

import com.arnor4eck.springkod.util.file.FileImpl;
import com.arnor4eck.springkod.util.file.loader.FileLoader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@AllArgsConstructor
public class ExportService {

    private final FileLoader fileLoader;

    public StreamingResponseBody export(long datasitoryId) throws FileNotFoundException {
        List<FileImpl> files = fileLoader.loadAll(datasitoryId);

        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                for (FileImpl file : files) {
                    addFileToZip(zipOut, file);
                }
            }
        };
    };

    private void addFileToZip(ZipOutputStream zipOut, FileImpl file) {
        try {
            ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
            zipOut.putNextEntry(zipEntry);
            zipOut.write(file.getBytes());
            zipOut.closeEntry();
        }catch (IOException e) {
            log.warn("Неудалось экспортировать файл {}", file.getOriginalFilename());
        }
    }
}
