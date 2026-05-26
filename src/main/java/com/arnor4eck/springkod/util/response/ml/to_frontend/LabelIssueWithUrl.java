package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LabelIssueWithUrl(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("url") String url,  // ✅ добавлено
        @JsonProperty("old_label") int oldLabel,
        @JsonProperty("old_label_name") String oldLabelName,
        @JsonProperty("suggested_label") int suggestedLabel,
        @JsonProperty("suggested_label_name") String suggestedLabelName
) {}
