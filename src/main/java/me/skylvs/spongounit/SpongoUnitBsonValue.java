package me.skylvs.spongounit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpongoUnitBsonValue {

    private Object value;
    private String bsonType;
    private String comparatorValue;

}
