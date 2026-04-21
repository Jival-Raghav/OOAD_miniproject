package com.insightinvest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightinvest.entity.Company;
import com.insightinvest.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CompanyDataService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;

    @Value("${insight.external.yahoo-chart-url}")
    private String yahooChartUrl;

    @Value("${insight.external.yahoo-summary-url}")
    private String yahooSummaryUrl;

    public CompanyDataService(ObjectMapper objectMapper, CompanyRepository companyRepository) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
        this.companyRepository = companyRepository;
    }

    public String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
    }

    public Map<String, String> resolveCompanyToSymbol(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            return Map.of("symbol", "", "name", "");
        }

        if (trimmed.matches("^[A-Za-z0-9.\\-]{1,15}$")) {
            return Map.of("symbol", trimmed.toUpperCase(Locale.ROOT), "name", trimmed);
        }

        try {
            String encoded = UriUtils.encode(trimmed, StandardCharsets.UTF_8);
            String response = webClient.get()
                    .uri("https://query2.finance.yahoo.com/v1/finance/search?q=" + encoded + "&quotesCount=5&newsCount=0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode quotes = root.path("quotes");
            if (quotes.isArray() && !quotes.isEmpty()) {
                JsonNode first = quotes.get(0);
                String symbol = first.path("symbol").asText(trimmed).toUpperCase(Locale.ROOT);
                String name = first.path("longname").asText(first.path("shortname").asText(trimmed));
                return Map.of("symbol", symbol, "name", name);
            }
        } catch (IOException | RuntimeException ignored) {
        }

        return Map.of("symbol", trimmed.toUpperCase(Locale.ROOT), "name", trimmed);
    }

    public List<Double> fetchHistoricalClosePrices(String symbol, String period) {
        String normalized = normalizeSymbol(symbol);
        String range = (period == null || period.isBlank()) ? "6mo" : period;
        String url = yahooChartUrl + "/" + normalized + "?range=" + range + "&interval=1d";

        try {
            String response = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
            JsonNode root = objectMapper.readTree(response);
            JsonNode closes = root.path("chart").path("result").get(0).path("indicators").path("quote").get(0).path("close");

            List<Double> prices = new ArrayList<>();
            if (closes.isArray()) {
                for (JsonNode node : closes) {
                    if (!node.isNull()) {
                        prices.add(node.asDouble());
                    }
                }
            }

            if (prices.size() < 20) {
                throw new IllegalStateException("Insufficient market data for symbol " + symbol);
            }
            return prices;
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Failed to fetch historical prices for " + symbol + ": " + ex.getMessage(), ex);
        }
    }

    public Map<String, Object> fetchFinancialMetrics(String symbol) {
        String normalized = normalizeSymbol(symbol);
        String url = yahooSummaryUrl + "/" + normalized + "?modules=price,summaryProfile,defaultKeyStatistics,financialData";

        Map<String, Object> metrics = new LinkedHashMap<>();
        try {
            String response = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
            JsonNode root = objectMapper.readTree(response).path("quoteSummary").path("result").get(0);
            JsonNode price = root.path("price");
            JsonNode profile = root.path("summaryProfile");
            JsonNode stats = root.path("defaultKeyStatistics");
            JsonNode financialData = root.path("financialData");

            metrics.put("company_name", textOrDefault(price.path("longName"), normalized));
            metrics.put("symbol", normalized);
            metrics.put("sector", textOrDefault(profile.path("sector"), "N/A"));
            metrics.put("industry", textOrDefault(profile.path("industry"), "N/A"));
            metrics.put("currency", textOrDefault(price.path("currency"), normalized.endsWith(".NS") ? "INR" : "USD"));
            metrics.put("current_price", nestedRaw(price.path("regularMarketPrice")));
            metrics.put("market_cap", nestedRaw(price.path("marketCap")));
            metrics.put("pe_ratio", nestedRaw(stats.path("trailingPE")));
            metrics.put("forward_pe", nestedRaw(stats.path("forwardPE")));
            metrics.put("eps", nestedRaw(stats.path("trailingEps")));
            metrics.put("debt_to_equity", nestedRaw(financialData.path("debtToEquity")));
            metrics.put("revenue_trend", textOrDefault(financialData.path("revenueGrowth").path("fmt"), "N/A"));
            metrics.put("profit_margin_trend", textOrDefault(financialData.path("profitMargins").path("fmt"), "N/A"));

            upsertCompany(normalized, String.valueOf(metrics.get("company_name")), String.valueOf(metrics.get("sector")), String.valueOf(metrics.get("industry")));
            return metrics;
        } catch (IOException | RuntimeException ex) {
            metrics.put("company_name", normalized);
            metrics.put("symbol", normalized);
            metrics.put("sector", "N/A");
            metrics.put("industry", "N/A");
            metrics.put("currency", normalized.endsWith(".NS") ? "INR" : "USD");
            metrics.put("current_price", 0.0);
            metrics.put("market_cap", 0.0);
            metrics.put("pe_ratio", 0.0);
            metrics.put("forward_pe", 0.0);
            metrics.put("eps", 0.0);
            metrics.put("debt_to_equity", 0.0);
            metrics.put("revenue_trend", "N/A");
            metrics.put("profit_margin_trend", "N/A");
            return metrics;
        }
    }

    private void upsertCompany(String symbol, String name, String sector, String industry) {
        Company company = companyRepository.findBySymbol(symbol).orElseGet(Company::new);
        company.setSymbol(symbol);
        company.setName(name == null || name.isBlank() ? symbol : name);
        company.setSector(sector);
        company.setIndustry(industry);
        companyRepository.save(company);
    }

    private static Object nestedRaw(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return 0.0;
        JsonNode raw = node.path("raw");
        if (!raw.isMissingNode() && !raw.isNull()) {
            return raw.isNumber() ? raw.numberValue() : raw.asText("0");
        }
        return node.isNumber() ? node.numberValue() : node.asText("0");
    }

    private static String textOrDefault(JsonNode node, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) return fallback;
        String value = node.asText("").trim();
        return value.isEmpty() ? fallback : value;
    }
}
