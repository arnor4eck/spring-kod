package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoadmapItem(@JsonProperty("id") int id,
                          @JsonProperty("action") String action) {
}
