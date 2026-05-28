package com.arnor4eck.springkod.util.dto.file;

import com.arnor4eck.springkod.entity.datasitory_file.DatasitoryFile;

public record BeatFileDto(String fileName) {
    public BeatFileDto(DatasitoryFile file) {
        this(file.getFileId().substring(file.getFileId().indexOf('-')));
    }
}
