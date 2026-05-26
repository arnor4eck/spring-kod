package com.arnor4eck.springkod.util.response.ml.to_frontend;

import com.arnor4eck.springkod.util.response.ml.RoadmapItem;
import com.arnor4eck.springkod.util.response.ml.Summary;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MlAnalyticsResponseWithUrls(
        @JsonProperty("summary") Summary summary,
        @JsonProperty("groups") GroupsWithUrls groups,
        @JsonProperty("roadmap") List<RoadmapItem> roadmap
) {}
