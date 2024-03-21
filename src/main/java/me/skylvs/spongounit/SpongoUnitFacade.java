package me.skylvs.spongounit;

import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.skylvs.spongounit.model.SpongoUnitCollection;
import org.opentest4j.AssertionFailedError;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SpongoUnitFacade {

    public static void assertMethod(MongoDatabase mongoDatabase, Annotation[] methodAnnotations) {
        val annotations = SpongoUnitAnnotationProcessor.processAnnotations(methodAnnotations);
        val datasets = SpongoUnitDatasetExtractor.extractDatasets(annotations);

        // Combine class and method seed datasets
        val expectedDataset = datasets.getExpectedDatasets();

        // Retrieve actual dataset from database
        List<SpongoUnitCollection> actualDataset = SpongoUnitDatabaseExtractor.extractFromDatabase(
                mongoDatabase,
                expectedDataset.stream().map(SpongoUnitCollection::getCollectionName).collect(Collectors.toList()).toArray(new String[0])
        );

        // Perform assertion
        AssertionResult assertionResult;
        try {
            assertionResult = SpongoUnitMatcher.assertDatasetsMatches(expectedDataset, actualDataset);
        } catch (Exception exception) {

            // Log error and rethrow
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        // If did not match, throw assertion error exception
        if (!assertionResult.isMatch()) {
            throw new AssertionFailedError(assertionResult.getMessage());
        }
    }

}
