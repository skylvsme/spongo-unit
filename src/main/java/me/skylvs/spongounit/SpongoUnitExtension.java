package me.skylvs.spongounit;

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.NotThreadSafe;

import static me.skylvs.spongounit.SpongoUnitAnnotationProcessor.isSpongoUnitSpec;

@NotThreadSafe
public class SpongoUnitExtension implements IGlobalExtension {

    @Override
    public void visitSpec(SpecInfo spec) {
        if (isSpongoUnitSpec(spec)) {
            SpongoUnitInterceptor interceptor = new SpongoUnitInterceptor(spec.getReflection());

            spec.addInitializerInterceptor(interceptor);
            spec.addSetupInterceptor(interceptor);
            spec.addCleanupInterceptor(interceptor);
        }
    }


}
