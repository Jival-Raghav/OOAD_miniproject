package com.insightinvest.dto;

import java.util.List;
import java.util.Map;

public class ForecastResponse {
    private String symbol;
    private String analysisTimestamp;
    private String version;
    private Map<String, Object> companyInfo;
    private List<Map<String, String>> newsHeadlines;
    private Map<String, Object> financialMetrics;
    private Map<String, Object> marketSentiment;
    private Map<String, Object> priceForecast;
    private String investmentReport;
    private Map<String, Object> visualization;
    private Map<String, Object> dataSources;
    private Map<String, Object> performanceMetrics;
    private String disclaimer;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getAnalysisTimestamp() { return analysisTimestamp; }
    public void setAnalysisTimestamp(String analysisTimestamp) { this.analysisTimestamp = analysisTimestamp; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Map<String, Object> getCompanyInfo() { return companyInfo; }
    public void setCompanyInfo(Map<String, Object> companyInfo) { this.companyInfo = companyInfo; }
    public List<Map<String, String>> getNewsHeadlines() { return newsHeadlines; }
    public void setNewsHeadlines(List<Map<String, String>> newsHeadlines) { this.newsHeadlines = newsHeadlines; }
    public Map<String, Object> getFinancialMetrics() { return financialMetrics; }
    public void setFinancialMetrics(Map<String, Object> financialMetrics) { this.financialMetrics = financialMetrics; }
    public Map<String, Object> getMarketSentiment() { return marketSentiment; }
    public void setMarketSentiment(Map<String, Object> marketSentiment) { this.marketSentiment = marketSentiment; }
    public Map<String, Object> getPriceForecast() { return priceForecast; }
    public void setPriceForecast(Map<String, Object> priceForecast) { this.priceForecast = priceForecast; }
    public String getInvestmentReport() { return investmentReport; }
    public void setInvestmentReport(String investmentReport) { this.investmentReport = investmentReport; }
    public Map<String, Object> getVisualization() { return visualization; }
    public void setVisualization(Map<String, Object> visualization) { this.visualization = visualization; }
    public Map<String, Object> getDataSources() { return dataSources; }
    public void setDataSources(Map<String, Object> dataSources) { this.dataSources = dataSources; }
    public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
