package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DuplicateFileWithUrl(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("url") String url  // ✅ добавлено
) {}
