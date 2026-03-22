package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.UserGender;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class UserGenderValidation implements ConstraintValidator<UserGender, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(UserGender constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.USER_GENDER_MALE)
                || Objects.equals(value, BaseConstant.USER_GENDER_FEMALE)
                || Objects.equals(value, BaseConstant.USER_GENDER_OTHER);
    }
}