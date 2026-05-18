package com.arnor4eck.springkod.util.validation.datasitory_role;

import com.arnor4eck.springkod.entity.datasitory_member.DatasitoryMemberRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DatasitoryRoleValidator implements ConstraintValidator<DatasitoryRoleValid, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try{
            DatasitoryMemberRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
