package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Groups(
        @JsonProperty("all_objects") List<AllObject> allObjects,
        @JsonProperty("reliable") List<ReliableObject> reliable,
        @JsonProperty("label_issues") List<LabelIssue> labelIssues,
        @JsonProperty("duplicates") List<DuplicateGroup> duplicates,
        @JsonProperty("quality_issues") List<QualityIssue> qualityIssues
) {
}
