package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClassInfo(
        @JsonProperty("class_idx") int classIdx,
        @JsonProperty("name") String name,
        @JsonProperty("count") int count,
        @JsonProperty("percentage") double percentage,
        @JsonProperty("deficit") double deficit
) {
}
