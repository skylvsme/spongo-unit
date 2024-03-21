package me.skylvs.spongounit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssertionResult {

    private boolean match;
    private String message;

}
