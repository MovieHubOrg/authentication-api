package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.SubtitleFontSizeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SubtitleFontSizeValidation implements ConstraintValidator<SubtitleFontSizeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(SubtitleFontSizeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.FONT_SIZE_SMALL)
                || Objects.equals(value, BaseConstant.FONT_SIZE_MEDIUM)
                || Objects.equals(value, BaseConstant.FONT_SIZE_LARGE);
    }
}
