package com.insightinvest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightinvest.dto.ForecastResult;
import com.insightinvest.dto.SentimentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;

@Service
public class AIAnalysisService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${insight.ai.groq-api-key:}")
    private String groqApiKey;

    @Value("${insight.ai.groq-model:llama-3.3-70b-versatile}")
    private String groqModel;

    public AIAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().baseUrl("https://api.groq.com/openai/v1").build();
    }

    public SentimentResult analyzeSentiment(List<String> headlines) {
        if (headlines == null || headlines.isEmpty()) {
            SentimentResult neutral = new SentimentResult();
            neutral.setSentiment("neutral");
            neutral.setConfidence(0.5);
            neutral.setSentimentScore(0.5);
            neutral.setSentimentDistribution(Map.of("positive", 0, "neutral", 0, "negative", 0));
            return neutral;
        }

        SentimentResult fromModel = sentimentViaGroq(headlines);
        if (fromModel != null) return fromModel;

        return sentimentHeuristic(headlines);
    }

    public String generateInvestmentReport(String symbol,
                                           Map<String, Object> metrics,
                                           SentimentResult sentiment,
                                           ForecastResult forecast,
                                           List<String> headlines) {
        String report = reportViaGroq(symbol, metrics, sentiment, forecast, headlines);
        if (report != null && !report.isBlank()) {
            return report;
        }

        double currentPrice = toDouble(metrics.get("current_price"));
        Double lastMean = forecast.getMean().isEmpty() ? null : forecast.getMean().get(forecast.getMean().size() - 1);
        double target = lastMean == null ? currentPrice : lastMean;
        double deltaPct = currentPrice == 0 ? 0 : ((target - currentPrice) / currentPrice) * 100.0;
        String recommendation = deltaPct > 5 ? "Moderate Buy" : deltaPct < -5 ? "Reduce" : "Hold";

        return "# Investment Outlook for " + symbol + "\n\n"
                + "## Snapshot\n"
                + "- Current Price: " + currentPrice + "\n"
                + "- Forecasted Price: " + target + "\n"
                + "- Forecast Change: " + String.format(Locale.ROOT, "%.2f", deltaPct) + "%\n"
                + "- Sentiment: " + sentiment.getSentiment() + "\n\n"
                + "## Recommendation\n"
                + recommendation + " based on blended trend, volatility, and headline sentiment.\n\n"
                + "## Notes\n"
                + "This report is generated for educational purposes only and is not investment advice.";
    }

    private SentimentResult sentimentViaGroq(List<String> headlines) {
        if (groqApiKey == null || groqApiKey.isBlank()) return null;

        String prompt = "Classify sentiment for these market headlines and return strict JSON with keys: sentiment, confidence, sentiment_score, sentiment_distribution.\n" + String.join("\n", headlines);

        Map<String, Object> request = Map.of(
                "model", groqModel,
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a financial sentiment classifier. Return only JSON."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode contentNode = objectMapper.readTree(response)
                    .path("choices").get(0).path("message").path("content");

            JsonNode parsed = objectMapper.readTree(contentNode.asText("{}"));
            SentimentResult out = new SentimentResult();
            out.setSentiment(parsed.path("sentiment").asText("neutral"));
            out.setConfidence(parsed.path("confidence").asDouble(0.6));
            out.setSentimentScore(parsed.path("sentiment_score").asDouble(0.5));

            Map<String, Integer> dist = new HashMap<>();
            JsonNode d = parsed.path("sentiment_distribution");
            dist.put("positive", d.path("positive").asInt(0));
            dist.put("neutral", d.path("neutral").asInt(0));
            dist.put("negative", d.path("negative").asInt(0));
            out.setSentimentDistribution(dist);
            return out;
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private String reportViaGroq(String symbol,
                                 Map<String, Object> metrics,
                                 SentimentResult sentiment,
                                 ForecastResult forecast,
                                 List<String> headlines) {
        if (groqApiKey == null || groqApiKey.isBlank()) return null;

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("symbol", symbol);
        context.put("metrics", metrics);
        context.put("sentiment", sentiment);
        context.put("forecast", forecast);
        context.put("headlines", headlines.stream().limit(8).toList());

        String prompt = "Generate a concise professional investment report in markdown with sections: Summary, Metrics, Risks, Recommendation. Data: " + context;

        Map<String, Object> request = Map.of(
                "model", groqModel,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a senior financial analyst."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readTree(response)
                    .path("choices").get(0).path("message").path("content")
                    .asText("");
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private SentimentResult sentimentHeuristic(List<String> headlines) {
        Set<String> positiveWords = Set.of("gain", "growth", "beat", "up", "strong", "surge", "bull", "optimistic");
        Set<String> negativeWords = Set.of("loss", "drop", "miss", "down", "weak", "fall", "bear", "risk");

        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (String h : headlines) {
            String lower = h.toLowerCase(Locale.ROOT);
            int p = positiveWords.stream().mapToInt(w -> lower.contains(w) ? 1 : 0).sum();
            int n = negativeWords.stream().mapToInt(w -> lower.contains(w) ? 1 : 0).sum();
            if (p > n) positive++;
            else if (n > p) negative++;
            else neutral++;
        }

        double total = Math.max(1, positive + negative + neutral);
        double score = (positive + 0.5 * neutral) / total;

        SentimentResult out = new SentimentResult();
        out.setSentiment(score > 0.6 ? "positive" : score < 0.4 ? "negative" : "neutral");
        out.setConfidence(Math.min(0.95, 0.55 + Math.abs(score - 0.5)));
        out.setSentimentScore(score);
        out.setSentimentDistribution(Map.of("positive", positive, "neutral", neutral, "negative", negative));
        return out;
    }

    private static double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
