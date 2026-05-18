package com.arnor4eck.springkod.util.request;

import com.arnor4eck.springkod.util.validation.datasitory_role.DatasitoryRoleValid;
import jakarta.validation.constraints.NotBlank;

public record AddMemberToDatasitoryRequest(@NotBlank String memberEmail,
                                           @DatasitoryRoleValid String datasitoryRole) {
}
