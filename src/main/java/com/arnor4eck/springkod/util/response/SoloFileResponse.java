package com.arnor4eck.springkod.util.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SoloFileResponse(@JsonProperty("filename") String filename,
                                @JsonProperty("file_type") String fileType,
                                @JsonProperty("size") long size,
                                @JsonProperty("content_type") String contentType,
                                @JsonProperty("status") String status,
                                @JsonProperty("error") String error){}  // опционально