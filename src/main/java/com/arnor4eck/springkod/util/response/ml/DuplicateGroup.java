package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DuplicateGroup(@JsonProperty("group_id") int groupId,
                             @JsonProperty("primary") DuplicateFile primary,
                             @JsonProperty("copies") List<DuplicateFile> copies) {
}
