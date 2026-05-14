package com.arnor4eck.springkod.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DatasitoryTypeValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank
public @interface DatasitoryTypeValid {
    String message() default "Некорректный формат типа датазитория";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
