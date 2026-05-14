package com.arnor4eck.springkod.util.file.saver;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseContentSaver implements ContentSaver { // TODO Саша после реализации своей мега-задумки
    @Override
    public void save(FileSaveClass saveClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveAll(List<FileSaveClass> saveClasses) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
