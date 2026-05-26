package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QualityIssueWithUrl(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("url") String url,  // ✅ добавлено
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("quality_score") double qualityScore
) {}
