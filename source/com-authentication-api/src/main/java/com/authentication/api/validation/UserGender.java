package com.authentication.api.validation;

import com.authentication.api.validation.impl.UserGenderValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserGenderValidation.class)
@Documented
public @interface UserGender {
    boolean allowNull() default false;

    String message() default "Status is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
