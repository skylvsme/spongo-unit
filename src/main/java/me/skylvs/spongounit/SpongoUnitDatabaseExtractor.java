package me.skylvs.spongounit;

import com.mongodb.client.MongoDatabase;
import lombok.val;
import me.skylvs.spongounit.model.SpongoUnitCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.skylvs.spongounit.BsonDataExtractor.getDocuments;

public class SpongoUnitDatabaseExtractor {

    public static List<SpongoUnitCollection> extractFromDatabase(MongoDatabase mongoDatabase, String... collections) throws IllegalArgumentException {

        val extractedCollections = new ArrayList<SpongoUnitCollection>();

        val collectionsToExtract = getCollectionNamesToUse(mongoDatabase, collections);

        // Extract documents from each collection
        for (val collectionName : collectionsToExtract) {

            val collection = mongoDatabase.getCollection(collectionName);

            // Extract documents (comprised of name/value maps) from single DB collection
            val documents = getDocuments(collection);

            // Create collection and add it to the list
            val spongoUnitCollection = SpongoUnitCollection.builder()
                    .collectionName(collectionName)
                    .documents(documents)
                    .build();
            extractedCollections.add(spongoUnitCollection);
        }

        return extractedCollections;
    }



    private static List<String> getCollectionNamesToUse(
            MongoDatabase mongoDatabase,
            String[] collectionNames) throws IllegalArgumentException {
        List<String> collectionNamesToExtract = new ArrayList<>();

        // Get names of all collections in db
        List<String> databaseCollectionNames = getDatabaseCollections(mongoDatabase);

        // If collectionNames is omitted, extract dataset from all collections
        if (collectionNames == null || collectionNames.length == 0) {
            collectionNamesToExtract.addAll(databaseCollectionNames);
        } else {
            for (String collectionName : collectionNames) {
                if (databaseCollectionNames.contains(collectionName)) {
                    collectionNamesToExtract.add(collectionName);
                } else {
                    val message = String.format("Specified collection '%s' does not exist in the database '%s'.",
                            collectionName,
                            mongoDatabase.getName()
                    );
                    throw new IllegalArgumentException(message);
                }
            }
        }
        return collectionNamesToExtract;
    }

    private static List<String> getDatabaseCollections(MongoDatabase mongoDatabase) {
        return StreamSupport.stream(mongoDatabase.listCollectionNames().spliterator(), false).collect(Collectors.toList());
    }

}
