package com.arnor4eck.springkod.util.request;

import com.arnor4eck.springkod.util.validation.datasitory_role.DatasitoryRoleValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AddMemberToDatasitoryRequest(@Positive long datasitoryId,
                                           @NotBlank String memberEmail,
                                           @DatasitoryRoleValid String datasitoryRole) {
}
