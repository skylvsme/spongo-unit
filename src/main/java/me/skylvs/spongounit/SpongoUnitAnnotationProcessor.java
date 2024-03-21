package me.skylvs.spongounit;

import lombok.extern.slf4j.Slf4j;
import me.skylvs.spongounit.annotation.ExpectedDataset;
import me.skylvs.spongounit.annotation.ExpectedDatasets;
import me.skylvs.spongounit.annotation.SeedDataset;
import me.skylvs.spongounit.annotation.SeedDatasets;
import me.skylvs.spongounit.model.SpongoUnitAnnotations;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Slf4j
public class SpongoUnitAnnotationProcessor {

    public static SpongoUnitAnnotations processAnnotations(Annotation[] allAnnotations) {
        SpongoUnitAnnotations annotations = new SpongoUnitAnnotations();

        for (Annotation annotation : allAnnotations) {
            if (annotation instanceof SeedDataset) {
                annotations.addSeedDatasetAnnotation((SeedDataset) annotation);
            } else if (annotation instanceof SeedDatasets) {
                annotations.addSeedDatasetAnnotations(((SeedDatasets) annotation).value());
            } else if (annotation instanceof ExpectedDataset) {
                annotations.addExpectedDatasetAnnotation((ExpectedDataset) annotation);
            } else if (annotation instanceof ExpectedDatasets) {
                annotations.addExpectedDatasetAnnotations(((ExpectedDatasets) annotation).value());
            }
        }

        return annotations;
    }

    public static Boolean isSpongoUnitMethod(MethodInfo method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(annotation ->
                annotation instanceof SeedDataset
                        || annotation instanceof SeedDatasets
                        || annotation instanceof ExpectedDataset
                        || annotation instanceof ExpectedDatasets);
    }

    public static Boolean isSpongoUnitSpec(SpecInfo spec) {
        return ReflectionUtil.isAnnotationPresentRecursive(spec.getReflection(), SpongoUnit.class);
    }

}
