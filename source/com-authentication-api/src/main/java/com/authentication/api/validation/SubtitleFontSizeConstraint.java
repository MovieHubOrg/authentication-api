package com.authentication.api.validation;

import com.authentication.api.validation.impl.SubtitleFontSizeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SubtitleFontSizeValidation.class)
@Documented
public @interface SubtitleFontSizeConstraint {
    boolean allowNull() default false;

    String message() default "Subtitle font size is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
