package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.util.file.FileImpl;

/**
 * Сохраняет файл в БД
 * */
public interface FileSaver {
    void save(FileImpl file, String key);
}
