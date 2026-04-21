package com.insightinvest.service;

import com.insightinvest.dto.NewsItem;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    @Value("${insight.external.google-news-rss-url}")
    private String googleNewsRssUrl;

    public List<NewsItem> fetchNews(String query, int maxItems) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String q = UriUtils.encodeQueryParam(query, StandardCharsets.UTF_8);
            String url = googleNewsRssUrl + "?q=" + q + "&hl=en-IN&gl=IN&ceid=IN:en";

            SyndFeedInput input = new SyndFeedInput();
            try (XmlReader reader = new XmlReader(new URL(url))) {
                SyndFeed feed = input.build(reader);
                int count = 0;
                for (SyndEntry entry : feed.getEntries()) {
                    if (count >= maxItems) break;
                    items.add(new NewsItem(entry.getTitle(), entry.getLink()));
                    count++;
                }
            }
        } catch (IOException | FeedException ignored) {
        }
        return items;
    }
}
