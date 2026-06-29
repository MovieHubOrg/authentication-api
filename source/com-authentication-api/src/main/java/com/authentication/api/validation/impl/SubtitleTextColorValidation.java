package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.SubtitleTextColorConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SubtitleTextColorValidation implements ConstraintValidator<SubtitleTextColorConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(SubtitleTextColorConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.TEXT_COLOR_YELLOW)
                || Objects.equals(value, BaseConstant.TEXT_COLOR_WHITE)
                || Objects.equals(value, BaseConstant.TEXT_COLOR_BLACK);
    }
}
