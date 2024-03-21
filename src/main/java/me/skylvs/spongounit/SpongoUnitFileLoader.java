package me.skylvs.spongounit;

import me.skylvs.spongounit.exception.SpongoUnitException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpongoUnitFileLoader {

    public static String retrieveResourceFromFile(String location) throws SpongoUnitException {

        String resourceContents = null;

        // Check if location starts with "/" and, if not, add it
        if (location.charAt(0) != '/') {
            location = "/" + location;
        }

        try {
            Path path = Paths.get(SpongoUnitFileLoader.class.getResource(location).toURI());
            resourceContents = new String(Files.readAllBytes(path));
        } catch (Exception exception) {

            //String testClassNamePath = getTestClassNamePath(relativePackageClass);

            String message = "Failed to load file resource at location '" + location + "', ";
            throw new SpongoUnitException(message, exception);
        }

        return resourceContents;
    }
}
