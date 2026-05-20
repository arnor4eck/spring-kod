package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.util.file.FileImpl;

import java.io.FileNotFoundException;

/**
 * Выгружает файл в БД
 * */
public interface FileLoader {
    FileImpl load(String key) throws FileNotFoundException;
}
