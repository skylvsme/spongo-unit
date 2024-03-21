package me.skylvs.spongounit.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(ExpectedDatasets.class)
public @interface ExpectedDataset {

    @AliasFor("path")
    String value();

    @AliasFor("value")
    String path();



}
