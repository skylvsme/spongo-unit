package me.skylvs.spongounit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.skylvs.spongounit.annotation.ExpectedDataset;
import me.skylvs.spongounit.annotation.SeedDataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder of discovered {@link SeedDataset} and {@link ExpectedDataset} annotations on a class or method.
 */
@Data
@AllArgsConstructor
public class SpongoUnitAnnotations {

    /**
     * List of {@link SeedDataset} annotations discovered on a test class or method.
     */
    private List<SeedDataset> seedDatasetAnnotations;

    /**
     * List of {@link ExpectedDataset} annotations discovered on a test class or method.
     */
    private List<ExpectedDataset> expectedDatasetAnnotations;

    /**
     * Default constructor.
     */
    public SpongoUnitAnnotations() {
        seedDatasetAnnotations = new ArrayList<>();
        expectedDatasetAnnotations = new ArrayList<>();
    }

    /**
     * Adds the provided 'seedDatasetAnnotation' to the list of {@link SeedDataset}
     * annotations.
     *
     * @param seedDatasetAnnotation {@link SeedDataset} annotation to add to the list.
     */
    public void addSeedDatasetAnnotation(SeedDataset seedDatasetAnnotation) {
        if (seedDatasetAnnotations == null) {
            seedDatasetAnnotations = new ArrayList<>();
        }

        seedDatasetAnnotations.add(seedDatasetAnnotation);
    }

    /**
     * Adds the provided 'seedDatasetAnnotations' to the list of {@link SeedDataset}
     * annotations.
     *
     * @param seedDatasetAnnotations Array of {@link SeedDataset} annotations to add to the
     *                                   list.
     */
    public void addSeedDatasetAnnotations(SeedDataset... seedDatasetAnnotations) {
        if (this.seedDatasetAnnotations == null) {
            this.seedDatasetAnnotations = new ArrayList<>();
        }

        Collections.addAll(this.seedDatasetAnnotations, seedDatasetAnnotations);
    }

    /**
     * Adds the provided 'expectedDatasetAnnotations' to the list of {@link ExpectedDataset}
     * annotations.
     *
     * @param expectedDatasetAnnotations {@link ExpectedDataset} annotation to add to the
     *                                       list.
     */
    public void addExpectedDatasetAnnotation(ExpectedDataset expectedDatasetAnnotations) {
        if (this.expectedDatasetAnnotations == null) {
            this.expectedDatasetAnnotations = new ArrayList<>();
        }

        this.expectedDatasetAnnotations.add(expectedDatasetAnnotations);
    }

    /**
     * Adds the provided 'expectedDatasetAnnotations' to the list of {@link ExpectedDataset}
     * annotations.
     *
     * @param expectedDatasetAnnotations Array of {@link ExpectedDataset} annotations to add
     *                                        to the list.
     */
    public void addExpectedDatasetAnnotations(ExpectedDataset... expectedDatasetAnnotations) {
        if (this.expectedDatasetAnnotations == null) {
            this.expectedDatasetAnnotations = new ArrayList<>();
        }

        Collections.addAll(this.expectedDatasetAnnotations, expectedDatasetAnnotations);
    }

}
