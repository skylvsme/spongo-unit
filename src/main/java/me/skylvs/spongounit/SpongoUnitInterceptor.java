package me.skylvs.spongounit;

import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.test.context.TestContextManager;

@Slf4j
public class SpongoUnitInterceptor extends AbstractMethodInterceptor {

    private final Class<?> specificationReflection;

    private MongoDatabase mongoDatabase;

    public SpongoUnitInterceptor(Class<?> specificationReflection) {
        this.specificationReflection = specificationReflection;
    }

    @Override
    public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed();

        TestContextManager contextManager = new TestContextManager(specificationReflection);
        contextManager.prepareTestInstance(specificationReflection);

        MongoDatabaseFactory mongoDatabaseFactory = contextManager.getTestContext().getApplicationContext().getBean(MongoDatabaseFactory.class);
        this.mongoDatabase = mongoDatabaseFactory.getMongoDatabase();
    }

    @Override
    public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        // Clear all collections out of the database
        Document emptyFilter = new Document();
        for (String collectionName : mongoDatabase.listCollectionNames()) {

            mongoDatabase.getCollection(collectionName).deleteMany(emptyFilter);

            log.trace("Cleared collection " + collectionName);
        }
        invocation.proceed();
    }

    @Override
    public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        System.out.println("Intercepted cleanup");

        if (SpongoUnitAnnotationProcessor.isSpongoUnitMethod(invocation.getMethod())) {
            SpongoUnitFacade.assertMethod(mongoDatabase, invocation.getMethod().getAnnotations());
        }

        invocation.proceed();
    }
}
