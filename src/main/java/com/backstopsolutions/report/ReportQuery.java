package com.backstopsolutions.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportQuery {
    private String queryDefinition;
    private String restrictionExpression;
    private String asOf;
}
