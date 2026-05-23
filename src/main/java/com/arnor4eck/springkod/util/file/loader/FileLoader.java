package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.entity.datasitory_file.FileType;
import com.arnor4eck.springkod.util.file.FileImpl;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Выгружает файл в БД
 * */
public interface FileLoader {
    FileImpl load(String key) throws FileNotFoundException;
    FileImpl load(long datasitoryId, FileType fileType) throws FileNotFoundException;
    List<FileImpl> loadAll(long datasitoryId) throws FileNotFoundException;
}
