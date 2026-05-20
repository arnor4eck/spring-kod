package com.arnor4eck.springkod.util.file.saver;

import java.util.List;

/**
 * Сохраняет содержимое и метаданные файла
 * */
interface ContentSaver {
    void save(FileSaveClass saveClass);
    void saveAll(List<FileSaveClass> saveClasses);
}
