package com.santik.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AggregatedResult {
    private Map<String, Double> pricing;
    private Map<String, String> track;
    private Map<String, List<String>> shipments;
}
