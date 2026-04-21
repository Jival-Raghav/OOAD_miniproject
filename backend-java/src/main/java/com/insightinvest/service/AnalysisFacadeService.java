package com.insightinvest.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.springframework.stereotype.Service;

import com.insightinvest.dto.ForecastResponse;
import com.insightinvest.dto.ForecastResult;
import com.insightinvest.dto.NewsItem;
import com.insightinvest.dto.SentimentResult;
import com.insightinvest.factory.ForecastResponseFactory;

@Service
public class AnalysisFacadeService {

    private final CompanyDataService companyDataService;
    private final NewsService newsService;
    private final ForecastService forecastService;
    private final AIAnalysisService aiAnalysisService;
    private final RiskAssessmentService riskAssessmentService;
    private final ReportGenerationService reportGenerationService;
    private final ForecastResponseFactory forecastResponseFactory;

    public AnalysisFacadeService(CompanyDataService companyDataService,
                                 NewsService newsService,
                                 ForecastService forecastService,
                                 AIAnalysisService aiAnalysisService,
                                 RiskAssessmentService riskAssessmentService,
                                 ReportGenerationService reportGenerationService,
                                 ForecastResponseFactory forecastResponseFactory) {
        this.companyDataService = companyDataService;
        this.newsService = newsService;
        this.forecastService = forecastService;
        this.aiAnalysisService = aiAnalysisService;
        this.riskAssessmentService = riskAssessmentService;
        this.reportGenerationService = reportGenerationService;
        this.forecastResponseFactory = forecastResponseFactory;
    }

    public ForecastResponse buildComprehensiveForecast(String inputSymbol, int steps, String period, int newsItems) {
        String symbol = inputSymbol;
        String companyName = inputSymbol;

        if (!inputSymbol.matches("^[A-Za-z0-9.\\-]{1,15}$")) {
            Map<String, String> resolved = companyDataService.resolveCompanyToSymbol(inputSymbol);
            symbol = resolved.getOrDefault("symbol", inputSymbol);
            companyName = resolved.getOrDefault("name", inputSymbol);
        }

        symbol = companyDataService.normalizeSymbol(symbol);

        Map<String, Object> financialMetrics = companyDataService.fetchFinancialMetrics(symbol);
        List<Double> historicalPrices = companyDataService.fetchHistoricalClosePrices(symbol, period);

        List<NewsItem> news = newsService.fetchNews(companyName + " OR " + symbol, newsItems);
        List<String> headlines = news.stream().map(NewsItem::getTitle).filter(Objects::nonNull).toList();

        SentimentResult sentiment = aiAnalysisService.analyzeSentiment(headlines);
        ForecastResult forecast = forecastService.generateForecast(historicalPrices, steps, sentiment.getSentimentScore());
        Map<String, Object> risk = riskAssessmentService.assessRisk(symbol, historicalPrices, sentiment.getSentimentScore());
        String chartBase64 = createChartBase64(historicalPrices, forecast);

        String report = aiAnalysisService.generateInvestmentReport(symbol, financialMetrics, sentiment, forecast, headlines);
        String recommendation = deriveRecommendation(financialMetrics, forecast);
        reportGenerationService.saveReport(symbol, report, recommendation);

        return forecastResponseFactory.create(
            symbol,
            companyName,
            financialMetrics,
            news,
            sentiment,
            forecast,
            report,
            chartBase64,
            risk,
            steps
        );
    }

    private static String deriveRecommendation(Map<String, Object> metrics, ForecastResult forecast) {
        double current = toDouble(metrics.get("current_price"));
        Double lastMean = forecast.getMean().isEmpty() ? null : forecast.getMean().get(forecast.getMean().size() - 1);
        double target = current;
        if (lastMean != null) {
            target = lastMean;
        }
        if (current <= 0) return "HOLD";
        double change = ((target - current) / current) * 100.0;
        if (change > 8) return "BUY";
        if (change < -8) return "REDUCE";
        return "HOLD";
    }

    private static double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private static String createChartBase64(List<Double> historical, ForecastResult forecast) {
        try {
            int histStart = Math.max(0, historical.size() - 60);
            List<Double> histSlice = historical.subList(histStart, historical.size());
            List<Integer> histX = IntStream.range(0, histSlice.size()).boxed().toList();
            List<Integer> forecastX = IntStream.range(histSlice.size(), histSlice.size() + forecast.getMean().size()).boxed().toList();

            XYChart chart = new XYChartBuilder()
                    .width(1000)
                    .height(500)
                    .title("Price Forecast")
                    .xAxisTitle("Time")
                    .yAxisTitle("Price")
                    .build();

            chart.addSeries("Historical", histX, histSlice);
            chart.addSeries("Forecast", forecastX, forecast.getMean());
            chart.addSeries("Lower", forecastX, forecast.getLower());
            chart.addSeries("Upper", forecastX, forecast.getUpper());

            byte[] png = BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
            return Base64.getEncoder().encodeToString(png);
        } catch (IOException ex) {
            return "";
        }
    }
}
