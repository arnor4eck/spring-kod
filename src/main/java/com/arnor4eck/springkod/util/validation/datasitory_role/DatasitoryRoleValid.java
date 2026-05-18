package com.arnor4eck.springkod.util.validation.datasitory_role;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DatasitoryRoleValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
public @interface DatasitoryRoleValid {
    String message() default "Некорректный формат роли участника";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

