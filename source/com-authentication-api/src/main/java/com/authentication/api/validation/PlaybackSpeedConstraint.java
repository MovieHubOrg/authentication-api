package com.authentication.api.validation;

import com.authentication.api.validation.impl.PlaybackSpeedValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlaybackSpeedValidation.class)
@Documented
public @interface PlaybackSpeedConstraint {
    boolean allowNull() default false;

    String message() default "Playback speed is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
