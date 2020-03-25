package org.comroid.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a VarArg parameter as an optional parameter, expecting this behavior: - The Parameter can
 * be left out -> Expects the method to provide a default value - The Parameter can be defined with
 * 1 element - The Method accepts a VarArgs array with up to {@link #limit()} elements
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface OptionalVararg {
    int limit() default 1;
}
