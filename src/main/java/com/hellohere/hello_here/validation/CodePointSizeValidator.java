package com.hellohere.hello_here.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CodePointSizeValidator implements ConstraintValidator<CodePointSize, String> {

    private int min;
    private int max;

    @Override
    public void initialize(CodePointSize constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();

        if (min < 0 || max < min) {
            throw new IllegalArgumentException("CodePointSizeの範囲指定が不正です");
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int length = value.codePointCount(0, value.length());
        return length >= min && length <= max;
    }
}
