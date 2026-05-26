package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AllObjectWithUrl(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("url") String url,  // ✅ добавлено
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("utility_score") double utilityScore,
        @JsonProperty("entropy") double entropy,
        @JsonProperty("confidence") double confidence,
        @JsonProperty("label_score") double labelScore,
        @JsonProperty("outlier_score") double outlierScore
) {}
