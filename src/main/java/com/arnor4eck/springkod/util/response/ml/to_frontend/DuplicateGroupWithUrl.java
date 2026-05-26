package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DuplicateGroupWithUrl(
        @JsonProperty("group_id") int groupId,
        @JsonProperty("primary") DuplicateFileWithUrl primary,
        @JsonProperty("copies") List<DuplicateFileWithUrl> copies
) {}
