package me.skylvs.spongounit;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.skylvs.spongounit.exception.SpongoUnitException;
import me.skylvs.spongounit.model.SpongoUnitCollection;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static me.skylvs.spongounit.BsonValueOperations.*;

@Slf4j
public class SpongoUnitMatcher {

    public static AssertionResult assertDatasetsMatches(
            List<SpongoUnitCollection> expectedDataset,
            List<SpongoUnitCollection> actualDataset
    ) {
        val actualMap = actualDataset.stream().collect(
                Collectors.toMap(SpongoUnitCollection::getCollectionName, e -> e)
        );

        for (SpongoUnitCollection expectedCollection : expectedDataset) {

            val expectedCollectionName = expectedCollection.getCollectionName();
            val actualCollection = actualMap.get(expectedCollectionName);

            // Assert such a collection is present in the actual
            if (actualCollection == null) {

                String message = "Expected collection " + expectedCollectionName + " to be present.";
                return new AssertionResult(false, message);
            }

            // Assert this collection matches; if doesn't match, return immediately
            AssertionResult singleCollectionAssertionResult;
            try {

                singleCollectionAssertionResult = assertSingleCollectionMatches(
                        expectedCollection,
                        actualCollection
                );

            } catch (SpongoUnitException e) {

                // Add tracing to the exception message
                String message = "Collection '" + expectedCollectionName + "': ";
                throw new SpongoUnitException(message + e.getMessage(), e);
            }

            // Return immediately if assertion failed
            if (!singleCollectionAssertionResult.isMatch()) {
                String message = "Collection '" + expectedCollectionName + "': "
                        + singleCollectionAssertionResult.getMessage();
                return new AssertionResult(false, message);
            }
        }

        return new AssertionResult(true, "Database state matches.");
    }

    public static AssertionResult assertSingleCollectionMatches(
            SpongoUnitCollection expected,
            SpongoUnitCollection actual
    ) throws SpongoUnitException {

        // Verify expected collection name is not null
        if (expected.getCollectionName() == null) {
            throw new SpongoUnitException("Expected collection name can not be 'null'.");
        }

        // Assert collection names match
        if (!expected.getCollectionName().equals(actual.getCollectionName())) {
            val message = String.format("Expected collection named '%s' but got '%s'",
                    expected.getCollectionName(),
                    actual.getCollectionName());
            return new AssertionResult(false, message);
        }

        List<Map<String, Object>> expectedDocuments = expected.getDocuments();
        List<Map<String, Object>> actualDocuments = actual.getDocuments();

        // Assert number of documents match
        if (expectedDocuments.size() != actualDocuments.size()) {
            val message = String.format("Expected %d documents in collection '%s' but got %s",
                    expectedDocuments.size(),
                    expected.getCollectionName(),
                    actualDocuments.size());
            return new AssertionResult(false, message);
        }

        // Run through expected documents and match with corresponding actual document
        for (int i = 0; i < expectedDocuments.size(); i++) {

            // Get same indexed expected and actual documents
            Map<String, Object> expectedDocument = expectedDocuments.get(i);
            Map<String, Object> actualDocument = actualDocuments.get(i);

            AssertionResult singleDocumentAssertionResult;
            try {
                singleDocumentAssertionResult = assertSingleDocumentMatches(
                        expectedDocument,
                        actualDocument
                );
            } catch (SpongoUnitException e) {
                String message = "Document array index of '" + i + "', expected document of "
                        + expectedDocument + " : ";
                throw new SpongoUnitException(message + e.getMessage(), e);
            }

            if (!singleDocumentAssertionResult.isMatch()) {
                String message = "Document '" + actualDocument + "': "
                        + singleDocumentAssertionResult.getMessage();
                return new AssertionResult(false, message);
            }
        }

        return new AssertionResult(true, "Collections match.");
    }

    public static AssertionResult assertSingleDocumentMatches(
            Map<String, Object> expectedDocument,
            Map<String, Object> actualDocument) throws SpongoUnitException {

        // Loop through all expected field names and check for match in actual
        Set<String> expectedFieldNames = expectedDocument.keySet();
        for (String expectedFieldName : expectedFieldNames) {

            // Assert field with the same exists in actual
            Object actualValue = actualDocument.get(expectedFieldName);
            if (actualValue == null) {

                String message = "Expected field name '" + expectedFieldName + "' to be present.";
                return new AssertionResult(false, message);
            }

            Object expectedValue = expectedDocument.get(expectedFieldName);

            // Assert values match
            AssertionResult singleValueAssertionResult;
            try {

                singleValueAssertionResult = assertSingleValueMatches(expectedValue, actualValue);

            } catch (SpongoUnitException e) {

                // Add tracing information
                String message = "Field name '" + expectedFieldName + "': ";
                throw new SpongoUnitException(message + e.getMessage(), e);
            }

            // Return immediately if assertion failed
            if (!singleValueAssertionResult.isMatch()) {
                String message = "Field name '" + expectedFieldName + "': "
                        + singleValueAssertionResult.getMessage();
                return new AssertionResult(false, message);
            }
        }

        return new AssertionResult(true, "Documents match.");
    }

    public static AssertionResult assertSingleValueMatches(
            Object expectedValue,
            Object actualValue
    ) throws SpongoUnitException {

        // Determine if expected value is a document
        if (expectedValue instanceof Map) {

            if (isBsonValue((Map<String, Object>) expectedValue)) {
                // Assert match using special Bson value comparator
                return assertBsonValueMatches((Map<String, Object>) expectedValue, actualValue);
            } else {

                // Assert actual value is also a document
                if (!(actualValue instanceof Map)) {
                    String message = "Expected a document but got '" + actualValue + "'.";
                    return new AssertionResult(false, message);
                }

                // Assert match as a regular document
                return assertSingleDocumentMatches(
                        (Map<String, Object>) expectedValue,
                        (Map<String, Object>) actualValue
                );
            }

        } else if (expectedValue instanceof List) {

            // Assert actual value is also a list
            if (!(actualValue instanceof List)) {
                String message = "Expected an array but got '" + actualValue + "'.";
                return new AssertionResult(false, message);
            }

            // Assert lists match
            //noinspection rawtypes
            return assertListMatches(
                    (List) expectedValue,
                    (List) actualValue
            );

        } else { // Anything other than Map or List

            // Try to cast expected
            //noinspection rawtypes
            Comparable comparableExpected = expectedToComparable(expectedValue, null);

            // Assert that actual is also not a Map or a List; if not, cast to Comparable
            if (actualValue instanceof Map || actualValue instanceof List) {
                String message = "Expected '" + expectedValue + "' but got '" + actualValue + "'.";
                return new AssertionResult(false, message);
            }

            //noinspection rawtypes
            Comparable comparableActual = actualToComparable(actualValue);

            // Compare expected and actual
            int comparison = compareNullable(comparableExpected, comparableActual);

            // Assert values match
            if (comparison != 0) {
                String message = "Expected '" + comparableExpected + "' to be equal to '"
                        + comparableActual + "'";
                return new AssertionResult(false, message);
            }

            return new AssertionResult(true, "Values match.");
        }
    }

    public static AssertionResult assertBsonValueMatches(
            Map<String, Object> expectedBsonValue,
            Object actualValue
    ) throws SpongoUnitException {
        // Extract values
        val bsonValue = BsonDataExtractor.extractBsonValue(expectedBsonValue, BSON_FIELD_NAME_INDICATOR);
        val bsonType = bsonValue.getBsonType();
        val expectedValue = bsonValue.getValue();

        String comparator = bsonValue.getComparatorValue();

        // Assume "=" if 'comparator' is null
        if (comparator == null) {
            comparator = "=";
        }

        // Throw exception if expected is null and comparator is not either "=" or "!="
        if (expectedValue == null && !comparator.equals("=") && !comparator.equals("!=")) {
            val message = "If expected value is specified as 'null', comparator must either be '=' or '!='.";
            log.error(message);
            throw new SpongoUnitException(message);
        }

        // Try to cast expected & actual values to Comparable
        Comparable<?> comparableExpected = expectedToComparable(expectedValue, bsonType);
        Comparable<?> comparableActual = actualToComparable(actualValue);

        // Compare expected and actual
        int comparison = compareNullable(comparableExpected, comparableActual);

        // Assert depending on the 'comparator' value set by developer
        switch (comparator) {
            case "=":
                if (comparison != 0) {
                    val message = String.format("Expected '%s' but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Values match.";
                    return new AssertionResult(true, message);
                }
            case "!=":
                if (comparison == 0) {
                    val message = String.format("Expected '%s' to be not equal to actual value but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Values are not equal as expected.";
                    return new AssertionResult(true, message);
                }

            case "<":
                if (comparison >= 0) {
                    val message = String.format("Expected '%s' to be less than actual but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Expected is less than actual as expected.";
                    return new AssertionResult(true, message);
                }

            case "<=":
                if (comparison > 0) {
                    val message = String.format("Expected '%s' to be less than or equal to actual but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Expected is less than or equal to actual as expected.";
                    return new AssertionResult(true, message);
                }

            case ">":
                if (comparison <= 0) {
                    val message = String.format("Expected '%s' to be greater than actual but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Expected is greater than actual as expected.";
                    return new AssertionResult(true, message);
                }

            case ">=":
                if (comparison < 0) {
                    val message = String.format("Expected '%s' to be greater or equal than actual but got '%s'.",
                            comparableExpected,
                            comparableActual
                    );
                    return new AssertionResult(false, message);
                } else {
                    val message = "Expected is greater than or equal to actual as expected.";
                    return new AssertionResult(true, message);
                }

            default:
                // Unsupported value provided for comparator
                val message = String.format( "Error: Comparator field '%s' value of '%s' is not supported.",
                        COMPARATOR_FIELD_NAME,
                        comparator
                );
                throw new SpongoUnitException(message);
        }
    }

    private static AssertionResult assertListMatches(
            List<?> expectedList,
            List<?> actualList
    ) {

        if (expectedList.size() != actualList.size()) {

            String message = "Expected array size of '" + expectedList.size() + "' but got '"
                    + actualList.size() + "'.";
            return new AssertionResult(false, message);
        }

        for (int i = 0; i < expectedList.size(); i++) {

            Object expectedValue = expectedList.get(i);
            Object actualValue = actualList.get(i);
            AssertionResult singleListValueAssertionResult = assertSingleValueMatches(expectedValue, actualValue);

            if (!singleListValueAssertionResult.isMatch()) {
                return singleListValueAssertionResult;
            }
        }

        return new AssertionResult(true, "Arrays match.");
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compareNullable(Comparable expected, Comparable actual) {

        // Treat expected = null, actual != null as expected < actual
        if (expected == null && actual != null) {
            return -1;
        }

        // Treat expected != null, actual = null as expected > actual
        if (expected != null && actual == null) {
            return 1;
        }

        if (expected == actual) {
            return 0;
        }

        return expected.compareTo(actual);
    }

}
