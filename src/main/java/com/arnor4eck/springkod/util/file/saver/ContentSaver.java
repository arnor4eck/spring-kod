package com.arnor4eck.springkod.util.file.saver;

import com.arnor4eck.springkod.util.file.FileImpl;

/**
 * Сохраняет содержимое и метаданные файла
 * */
interface ContentSaver {
    void save(FileImpl file, String key);
}
