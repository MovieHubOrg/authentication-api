package com.authentication.api.validation.impl;

import com.authentication.api.constant.BaseConstant;
import com.authentication.api.validation.GroupKind;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class GroupKindValidation implements ConstraintValidator<GroupKind, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(GroupKind constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.GROUP_KIND_USER)
                || Objects.equals(value, BaseConstant.GROUP_KIND_USER_VIP)
                || Objects.equals(value, BaseConstant.GROUP_KIND_ADMIN)
                || Objects.equals(value, BaseConstant.GROUP_KIND_MANAGER)
                || Objects.equals(value, BaseConstant.GROUP_KIND_EMPLOYEE);
    }
}