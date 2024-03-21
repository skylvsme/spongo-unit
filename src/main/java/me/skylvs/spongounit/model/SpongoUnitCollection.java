package me.skylvs.spongounit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class SpongoUnitCollection {

    private String collectionName;
    private List<Map<String, Object>> documents;

}
