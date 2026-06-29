package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.SubtitleBackgroundColorConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SubtitleBackgroundColorValidation implements ConstraintValidator<SubtitleBackgroundColorConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(SubtitleBackgroundColorConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.BG_COLOR_YELLOW)
                || Objects.equals(value, BaseConstant.BG_COLOR_WHITE)
                || Objects.equals(value, BaseConstant.BG_COLOR_BLACK)
                || Objects.equals(value, BaseConstant.BG_COLOR_NONE);
    }
}
