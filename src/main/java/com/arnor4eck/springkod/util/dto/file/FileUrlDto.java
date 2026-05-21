package com.arnor4eck.springkod.util.dto.file;

import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;

public record FileUrlDto(String fileName, String url) {
    public FileUrlDto(ImageUrl imageUrl) {
        this(imageUrl.getDatasitoryFile().getFileId(), imageUrl.getUrl());
    }
}
