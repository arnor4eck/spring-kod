package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MlAnalyticsResponse(
        @JsonProperty("summary") Summary summary,
        @JsonProperty("groups") Groups groups,
        @JsonProperty("roadmap") List<RoadmapItem> roadmap
) {
}
