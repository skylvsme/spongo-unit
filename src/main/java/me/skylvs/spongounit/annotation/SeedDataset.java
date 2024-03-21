package me.skylvs.spongounit.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(SeedDatasets.class)
public @interface SeedDataset {

    @AliasFor("path")
    String value();

    @AliasFor("value")
    String path();

}
