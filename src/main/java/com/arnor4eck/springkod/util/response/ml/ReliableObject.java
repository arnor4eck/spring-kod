package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReliableObject(@JsonProperty("file_name") String fileName,
                             @JsonProperty("tags") List<String> tags,
                             @JsonProperty("utility_score") double utilityScore) {
}
