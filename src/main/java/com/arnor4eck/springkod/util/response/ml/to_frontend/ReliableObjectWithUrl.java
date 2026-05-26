package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReliableObjectWithUrl(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("url") String url,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("utility_score") double utilityScore
) {}
