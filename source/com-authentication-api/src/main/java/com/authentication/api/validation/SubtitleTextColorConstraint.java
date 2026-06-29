package com.authentication.api.validation;

import com.authentication.api.validation.impl.SubtitleTextColorValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SubtitleTextColorValidation.class)
@Documented
public @interface SubtitleTextColorConstraint {
    boolean allowNull() default false;

    String message() default "Subtitle text color is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
