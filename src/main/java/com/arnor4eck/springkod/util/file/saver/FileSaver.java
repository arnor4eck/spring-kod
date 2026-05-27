package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.entity.datasitory.Datasitory;

import java.util.List;

/**
 * Сохраняет файл в БД
 * */
public interface FileSaver {
    void save(FileSaveClass saveClass, Datasitory datasitory);
    void saveAll(List<FileSaveClass> saveClass, Datasitory datasitory);
    void updateOnlyContent(FileSaveClass saveClass);
}
