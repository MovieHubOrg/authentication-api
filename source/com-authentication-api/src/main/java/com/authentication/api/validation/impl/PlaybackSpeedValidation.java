package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.PlaybackSpeedConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PlaybackSpeedValidation implements ConstraintValidator<PlaybackSpeedConstraint, Double> {
    private boolean allowNull;

    @Override
    public void initialize(PlaybackSpeedConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return BaseConstant.PLAYBACK_SPEEDS.contains(value);
    }
}
