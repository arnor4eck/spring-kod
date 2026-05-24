package com.arnor4eck.springkod.util.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MlAnaliticsResponse(@JsonProperty("files") List<SoloFileResponse> files,
                                @JsonProperty("count") long count,
                                @JsonProperty("images_count") long imagesCount,
                                @JsonProperty("has_markup") boolean hasMarkup) {}
