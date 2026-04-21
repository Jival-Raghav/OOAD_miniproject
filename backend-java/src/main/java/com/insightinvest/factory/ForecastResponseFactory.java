package com.insightinvest.factory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.insightinvest.dto.ForecastResponse;
import com.insightinvest.dto.ForecastResult;
import com.insightinvest.dto.NewsItem;
import com.insightinvest.dto.SentimentResult;

@Component
public class ForecastResponseFactory {

    public ForecastResponse create(String symbol,
                                   String companyName,
                                   Map<String, Object> financialMetrics,
                                   List<NewsItem> news,
                                   SentimentResult sentiment,
                                   ForecastResult forecast,
                                   String report,
                                   String chartBase64,
                                   Map<String, Object> risk,
                                   int steps) {
        ForecastResponse response = new ForecastResponse();
        response.setSymbol(symbol);
        response.setAnalysisTimestamp(OffsetDateTime.now().toString());
        response.setVersion("3.0.0-java");

        response.setCompanyInfo(Map.of(
                "name", String.valueOf(financialMetrics.getOrDefault("company_name", companyName)),
                "sector", String.valueOf(financialMetrics.getOrDefault("sector", "N/A")),
                "industry", String.valueOf(financialMetrics.getOrDefault("industry", "N/A")),
                "current_price", financialMetrics.getOrDefault("current_price", 0),
                "market_cap", financialMetrics.getOrDefault("market_cap", 0)
        ));

        List<Map<String, String>> newsObjects = (news == null ? List.<NewsItem>of() : news).stream()
                .filter(Objects::nonNull)
                .limit(5)
                .map(item -> Map.of(
                        "title", item.getTitle() == null ? "Untitled" : item.getTitle(),
                        "url", item.getUrl() == null ? "" : item.getUrl()
                ))
                .collect(Collectors.toList());
        response.setNewsHeadlines(newsObjects);

        response.setFinancialMetrics(financialMetrics);
        response.setMarketSentiment(Map.of(
                "source", "Google News RSS + Groq/heuristic sentiment",
                "analysis", Map.of(
                        "sentiment", sentiment.getSentiment(),
                        "confidence", sentiment.getConfidence(),
                        "sentiment_score", sentiment.getSentimentScore(),
                        "sentiment_distribution", sentiment.getSentimentDistribution()
                ),
                "sample_headlines", (news == null ? List.<NewsItem>of() : news).stream()
                        .map(NewsItem::getTitle)
                        .filter(Objects::nonNull)
                        .limit(5)
                        .toList(),
                "methodology", "Headline sentiment classification with fallback heuristic"
        ));

        response.setPriceForecast(Map.of(
                "mean", forecast.getMean(),
                "lower", forecast.getLower(),
                "upper", forecast.getUpper(),
                "diagnostics", forecast.getDiagnostics()
        ));
        response.setInvestmentReport(report);

        response.setVisualization(Map.of(
                "chart", chartBase64,
                "description", "Forecast chart generated in Java",
                "features", List.of("Historical prices", "Forecast mean", "Confidence bands")
        ));

        response.setDataSources(Map.of(
                "stock_data", "Yahoo Finance chart + summary endpoints",
                "news_sentiment", "Google News RSS",
                "financial_metrics", "Yahoo Finance quote summary",
                "forecast_model", "Trend + volatility + sentiment fusion (Java)"
        ));

        response.setPerformanceMetrics(Map.of(
                "forecast_steps", steps,
                "confidence_level", "95%",
                "sentiment_impact", forecast.getDiagnostics().getOrDefault("sentiment_adjustment", 1.0),
                "risk", risk
        ));

        response.setDisclaimer("This analysis is for informational and educational purposes only and is not financial advice.");
        return response;
    }
}
