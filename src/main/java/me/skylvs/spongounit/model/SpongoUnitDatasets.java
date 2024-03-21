package me.skylvs.spongounit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SpongoUnitDatasets {

    List<SpongoUnitCollection> seedDatasets;
    List<SpongoUnitCollection> expectedDatasets;

}
