package com.authentication.api.validation;

import com.authentication.api.validation.impl.SubtitleBackgroundColorValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SubtitleBackgroundColorValidation.class)
@Documented
public @interface SubtitleBackgroundColorConstraint {
    boolean allowNull() default false;

    String message() default "Subtitle background color is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
