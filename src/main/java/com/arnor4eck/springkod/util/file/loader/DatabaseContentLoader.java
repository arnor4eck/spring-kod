package com.arnor4eck.springkod.util.file.loader;

import com.arnor4eck.springkod.util.file.FileImpl;
import org.springframework.stereotype.Component;

@Component
public class DatabaseContentLoader implements ContentLoader{ // TODO Саша после реализации своей мега-задумки
    @Override
    public FileImpl load(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
