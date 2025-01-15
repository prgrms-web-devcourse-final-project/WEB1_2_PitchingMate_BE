package com.example.mate.common.util.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.domain.Sort.Direction;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ValidPageable {

    @AliasFor("size")
    int value() default 10;

    @AliasFor("value")
    int size() default 10;

    int page() default 0;

    String[] sort() default {};

    Direction direction() default Direction.ASC;
}
