package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.ResolutionConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ResolutionValidation implements ConstraintValidator<ResolutionConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(ResolutionConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.RESOLUTION_HIGHEST)
                || Objects.equals(value, BaseConstant.RESOLUTION_AUTO)
                || Objects.equals(value, BaseConstant.RESOLUTION_1440P)
                || Objects.equals(value, BaseConstant.RESOLUTION_1080P)
                || Objects.equals(value, BaseConstant.RESOLUTION_720P);
    }
}
