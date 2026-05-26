package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroupsWithUrls(
        @JsonProperty("all_objects") List<AllObjectWithUrl> allObjects,
        @JsonProperty("reliable") List<ReliableObjectWithUrl> reliable,
        @JsonProperty("label_issues") List<LabelIssueWithUrl> labelIssues,
        @JsonProperty("duplicates") List<DuplicateGroupWithUrl> duplicates,
        @JsonProperty("quality_issues") List<QualityIssueWithUrl> qualityIssues
) {}
