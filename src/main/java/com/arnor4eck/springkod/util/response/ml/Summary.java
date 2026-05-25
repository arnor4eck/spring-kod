package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Summary(
        @JsonProperty("readiness") int readiness,
        @JsonProperty("n_total") int nTotal,
        @JsonProperty("n_classes") int nClasses,
        @JsonProperty("classes") List<ClassInfo> classes
) {
}
