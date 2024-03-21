package me.skylvs.spongounit;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.skylvs.spongounit.exception.SpongoUnitException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Slf4j
class BsonValueOperations {

    public static final String BSON_FIELD_NAME_INDICATOR = "$$";
    public static final String COMPARATOR_FIELD_NAME = "comparator";
    public static final String DATE_STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected static Comparable<?> actualToComparable(Object actualValue) throws SpongoUnitException {
        try {
            return (Comparable<?>) actualValue;
        } catch (ClassCastException exception) {
            val message = String.format("Actual value of '%s' does not appears to be supported as Comparable. " +
                    "Expected type: '%s'",
                    actualValue,
                    actualValue.getClass().getTypeName()
            );
            log.error(message);
            throw new SpongoUnitException(message, exception);
        }
    }

    protected static Comparable<?> expectedToComparable(Object expectedValue, String bsonType)
            throws SpongoUnitException {

        // If expected value is null, always return 'null'
        if (expectedValue == null) {
            return null;
        }

        try {
            // If bsonType isn't specified, just need to cast to Comparable
            if (bsonType == null) {
                return (Comparable<?>) expectedValue;
            }

            switch (bsonType.trim()) {
                // Cases where return is as is because it's already Comparable
                case "DOUBLE":
                    return Double.parseDouble(expectedValue.toString());
                case "INT32":
                    return Integer.parseInt(expectedValue.toString());
                case "TIMESTAMP":
                case "INT64":
                    return Long.parseLong(expectedValue.toString());
                case "DECIMAL128":
                    return BigDecimal.valueOf(Double.parseDouble(expectedValue.toString()));
                case "":
                case "STRING":
                case "BOOLEAN":
                case "BINARY":
                case "OBJECT_ID":
                case "NULL":
                case "UNDEFINED":
                case "REGULAR_EXPRESSION":
                case "JAVASCRIPT":
                case "SYMBOL":
                case "JAVASCRIPT_WITH_SCOPE":
                    return (Comparable<?>) expectedValue;
                case "DATE_TIME":
                    try {
                        SimpleDateFormat format = new SimpleDateFormat(DATE_STRING_FORMAT);
                        Date date = format.parse((String) expectedValue);
                        return date.getTime();
                    } catch (ParseException e) {
                        val message = String.format("DateTime value was not in the supported format of '%s'. " +
                                "Tried to parse '%s'.",
                                DATE_STRING_FORMAT,
                                expectedValue
                        );
                        log.error(message);
                        throw new SpongoUnitException(message);
                    }
                default:
                    val message = String.format("BSON type '%s' is not currently supported by"
                            + " the SpongoUnit framework.",
                            bsonType
                    );
                    log.error(message);
                    throw new SpongoUnitException(message);
            }

        } catch (ClassCastException exception) {

            String message = String.format("Expected value of '%s' does not appears to be supported as Comparable. " +
                            "Expected type: '%s'",
                    expectedValue,
                    expectedValue.getClass().getTypeName()
            );

            if (bsonType != null && !bsonType.equals("")) {
                message += String.format(" Expected value's BSON type was specified to be '%s'.", bsonType);
            }

            log.error(message);
            throw new SpongoUnitException(message, exception);
        }
    }

    protected static boolean isBsonValue(Map<String, Object> value) {
        return value.keySet().stream().anyMatch(key -> key.startsWith(BSON_FIELD_NAME_INDICATOR));
    }

}
