package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.util.file.FileImpl;

import java.io.FileNotFoundException;

/**
 * Выгружает метаданные и содержимое файла
 * */
interface ContentLoader {
    FileImpl load(String key) throws FileNotFoundException;
}
