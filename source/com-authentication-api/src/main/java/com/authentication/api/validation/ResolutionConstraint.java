package com.authentication.api.validation;

import com.authentication.api.validation.impl.ResolutionValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResolutionValidation.class)
@Documented
public @interface ResolutionConstraint {
    boolean allowNull() default false;

    String message() default "Resolution is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
