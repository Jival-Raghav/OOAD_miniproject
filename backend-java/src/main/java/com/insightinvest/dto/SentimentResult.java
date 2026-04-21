package com.insightinvest.dto;

import java.util.Map;

public class SentimentResult {
    private String sentiment;
    private double confidence;
    private double sentimentScore;
    private Map<String, Integer> sentimentDistribution;

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }
    public Map<String, Integer> getSentimentDistribution() { return sentimentDistribution; }
    public void setSentimentDistribution(Map<String, Integer> sentimentDistribution) { this.sentimentDistribution = sentimentDistribution; }
}
