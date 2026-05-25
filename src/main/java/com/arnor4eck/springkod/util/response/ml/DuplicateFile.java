package com.arnor4eck.springkod.util.response.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DuplicateFile(@JsonProperty("file_name") String fileName) {
}
