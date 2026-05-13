package com.arnor4eck.springkod.util.request.datasitory;

import com.arnor4eck.springkod.util.validation.DatasitoryTypeValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDatasitoryRequest(@NotBlank String name,
                                      @NotBlank String description,
                                      @DatasitoryTypeValid String datasitoryType,
                                      @NotNull Long creatorId) {
}
