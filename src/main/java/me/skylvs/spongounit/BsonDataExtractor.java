package me.skylvs.spongounit;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import lombok.val;
import me.skylvs.spongounit.exception.SpongoUnitException;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;

import java.util.*;

import static me.skylvs.spongounit.BsonValueOperations.COMPARATOR_FIELD_NAME;

public class BsonDataExtractor {

    public static List<Map<String, Object>> getDocuments(MongoCollection<Document> mongoCollection) {

        List<Map<String, Object>> documents = new ArrayList<>();

        // Loop over each document in 'mongoCollection'
        FindIterable<Document> mongoDocuments = mongoCollection.find();
        for (Document document : mongoDocuments) {

            // Convert document to BSON document
            BsonDocument bsonDocument = document.toBsonDocument(
                    BsonDocument.class,
                    MongoClientSettings.getDefaultCodecRegistry()
            );

            // Extract all unit fields from this document and add them to document list as a map
            Map<String, Object> fields = getDocument(bsonDocument);
            documents.add(fields);
        }

        return documents;
    }

    private static Map<String, Object> getDocument(BsonDocument bsonDocument) {
        Map<String, Object> document = new HashMap<>();

        // Loop over all document fields
        Set<String> fieldKeys = bsonDocument.keySet();
        for (String fieldKey : fieldKeys) {

            // Get value for field key
            BsonValue bsonValue = bsonDocument.get(fieldKey);
            Object fieldValue = getFieldValue(bsonValue);

            // Store field key and its value in the map
            document.put(fieldKey, fieldValue);
        }

        return document;
    }

    private static Object getFieldValue(BsonValue bsonValue){

        // Extract value based on the BsonType
        switch (bsonValue.getBsonType()) {
            case ARRAY:
                return getArrayValues(bsonValue.asArray());

            case DOCUMENT:
                return getDocument(bsonValue.asDocument());

            case DOUBLE:
                return bsonValue.asDouble().getValue();

            case STRING:
                return bsonValue.asString().getValue();

            case BINARY:
                // Store using Base64 encoding
                return Base64.getEncoder().encodeToString(bsonValue.asBinary().getData());

            case OBJECT_ID:
                return bsonValue.asObjectId().getValue().toHexString();

            case BOOLEAN:
                return bsonValue.asBoolean().getValue();

            case DATE_TIME:
                return bsonValue.asDateTime().getValue();

            case NULL:
            case UNDEFINED:
                return null;

            case REGULAR_EXPRESSION:
                return bsonValue.asRegularExpression().getPattern();

            case DB_POINTER:
                String namespace = bsonValue.asDBPointer().getNamespace();
                String objectId = bsonValue.asObjectId().getValue().toHexString();

                Map<String, String> dbPointerValueMap = new HashMap<>();
                dbPointerValueMap.put("namespace", namespace);
                dbPointerValueMap.put("objectId", objectId);

                return dbPointerValueMap;

            case JAVASCRIPT:
                return bsonValue.asJavaScript().getCode();

            case SYMBOL:
                return bsonValue.asSymbol().getSymbol();

            case JAVASCRIPT_WITH_SCOPE:
                return bsonValue.asJavaScriptWithScope().getCode();

            case INT32:
                return bsonValue.asInt32().getValue();

            case TIMESTAMP:
                return bsonValue.asTimestamp().getValue();

            case INT64:
                return bsonValue.asInt64().getValue();

            case DECIMAL128:
                return bsonValue.asDecimal128().decimal128Value().bigDecimalValue();

            // END_OF_DOCUMENT, MIN_KEY, MAX_KEY
            default:
                val message = String.format("BSON type %s is not currently supported by the SpongoUnit.",
                        bsonValue.getBsonType()
                );
                throw new SpongoUnitException(message);
        }
    }

    public static Map<String, Object> generateSpongoUnitBsonValue(
            String fieldNameIndicator,
            String bsonType,
            Object value) {

        Map<String, Object> document = new HashMap<>();
        String key = fieldNameIndicator + bsonType;
        document.put(key, value);

        return document;
    }

    private static List<Object> getArrayValues(BsonArray bsonArrayValue) {

        List<Object> arrayValues = new ArrayList<>();

        // Loop over array values and extract each one
        for (BsonValue bsonValue : bsonArrayValue.getValues()) {

            // Extract value and add it to list of array values
            Object value = getFieldValue(bsonValue);
            arrayValues.add(value);
        }

        return arrayValues;
    }

    protected static SpongoUnitBsonValue extractBsonValue(
            Map<String, Object> bsonDocument,
            String fieldNameIndicator) throws SpongoUnitException {

        Set<String> allKeys = bsonDocument.keySet();

        // Find key that contains field name indicator
        String indicatorKey = allKeys
                .stream()
                .filter(key -> key.startsWith(fieldNameIndicator))
                .findAny()
                .orElseThrow(() -> {
                    val message = String.format(
                            "The following document was expected to have field name indicator '%s' " +
                                    "but didn't:\n'%s'.",
                            fieldNameIndicator,
                            bsonDocument
                    );
                    return new SpongoUnitException(message);
                });

        // Extract bson type; if not there, set it to null
        String bsonType = indicatorKey.substring(fieldNameIndicator.length());
        bsonType = bsonType.trim().length() == 0 ? null : bsonType;

        val value = bsonDocument.get(indicatorKey);
        val comparatorValue = (String) bsonDocument.get(COMPARATOR_FIELD_NAME);

        return SpongoUnitBsonValue.builder()
                .bsonType(bsonType)
                .value(value)
                .comparatorValue(comparatorValue)
                .build();
    }

}
