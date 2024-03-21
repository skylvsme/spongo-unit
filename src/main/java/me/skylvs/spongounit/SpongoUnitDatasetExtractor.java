package me.skylvs.spongounit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.skylvs.spongounit.annotation.ExpectedDataset;
import me.skylvs.spongounit.annotation.SeedDataset;
import me.skylvs.spongounit.exception.SpongoUnitException;
import me.skylvs.spongounit.model.SpongoUnitAnnotations;
import me.skylvs.spongounit.model.SpongoUnitCollection;
import me.skylvs.spongounit.model.SpongoUnitDatasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SpongoUnitDatasetExtractor {

    public static SpongoUnitDatasets extractDatasets(SpongoUnitAnnotations annotations) {
        val datasets = new SpongoUnitDatasets();

        val totalUncombinedSeedDataset = new ArrayList<SpongoUnitCollection>();
        val totalUncombinedAssertDataset = new ArrayList<SpongoUnitCollection>();

        // Process seed annotations
        for (SeedDataset annotation : annotations.getSeedDatasetAnnotations()) {
            val collections = processSeedWithDatasetAnnotation(annotation);
            totalUncombinedSeedDataset.addAll(collections);
        }

        // Process assert annotations
        for (ExpectedDataset expectedDatasetAnnotation :
                annotations.getExpectedDatasetAnnotations()) {
            val collections = processExpectedDatasets(expectedDatasetAnnotation);
            totalUncombinedAssertDataset.addAll(collections);
        }

        datasets.setSeedDatasets(totalUncombinedSeedDataset);
        datasets.setExpectedDatasets(totalUncombinedAssertDataset);

        return datasets;
    }

    private static List<SpongoUnitCollection> processSeedWithDatasetAnnotation(SeedDataset annotation) throws SpongoUnitException {

        val value = annotation.value();
        val path = annotation.path();

        val datasetCollections = new ArrayList<SpongoUnitCollection>();

        val fileLocations = Stream.of(value, path).toArray(String[]::new);

        // Loop over locations, retrieve dataset content and convert/collect to SpongoUnitCollection
        for (val fileLocation : fileLocations) {
            val dataset = SpongoUnitFileLoader.retrieveResourceFromFile(fileLocation);
            datasetCollections.addAll(parseDatasetFromJson(dataset));
        }

        return datasetCollections;
    }

    private static List<SpongoUnitCollection> processExpectedDatasets(ExpectedDataset annotation) {
        val value = annotation.value();
        val path = annotation.path();

        val datasetCollections = new ArrayList<SpongoUnitCollection>();

        val fileLocations = Stream.of(value, path).toArray(String[]::new);

        // Loop over locations, retrieve dataset content and convert/collect to SpongoUnitCollection
        for (val fileLocation : fileLocations) {
            val dataset = SpongoUnitFileLoader.retrieveResourceFromFile(fileLocation);
            datasetCollections.addAll(parseDatasetFromJson(dataset));
        }

        return datasetCollections;
    }

    private static List<SpongoUnitCollection> parseDatasetFromJson(String json) {
        try {
            ObjectMapper jsonMapper = new ObjectMapper();

            return jsonMapper.readValue(json, new TypeReference<List<SpongoUnitCollection>>() {
            });
        } catch (IOException exception) {
            String message = "Unable to interpret JSON dataset. " + exception.getMessage();
            log.error(message);
            throw new RuntimeException(message, exception);
        }
    }

}
