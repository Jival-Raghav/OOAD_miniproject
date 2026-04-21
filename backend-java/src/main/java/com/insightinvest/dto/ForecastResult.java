package com.insightinvest.dto;

import java.util.List;
import java.util.Map;

public class ForecastResult {
    private List<Double> mean;
    private List<Double> lower;
    private List<Double> upper;
    private Map<String, Object> diagnostics;

    public List<Double> getMean() { return mean; }
    public void setMean(List<Double> mean) { this.mean = mean; }
    public List<Double> getLower() { return lower; }
    public void setLower(List<Double> lower) { this.lower = lower; }
    public List<Double> getUpper() { return upper; }
    public void setUpper(List<Double> upper) { this.upper = upper; }
    public Map<String, Object> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(Map<String, Object> diagnostics) { this.diagnostics = diagnostics; }
}
