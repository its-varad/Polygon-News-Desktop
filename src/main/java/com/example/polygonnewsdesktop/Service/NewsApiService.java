package com.example.polygonnewsdesktop.Service;

import com.example.polygonnewsdesktop.Model.NewsArticle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NewsApiService {
    private static final String API_KEY="32544ba30a0646519745275e8b0ddda7";
    private static final String BASE_URL="https://newsapi.org/v2/top-headlines";

    public List<NewsArticle> fetchNews(String q,
                                       String category,
                                       String country,
                                       String language,
                                       String sortBy,
                                       int pageSize,
                                       int page) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("?apiKey=").append(API_KEY);
        if (q != null && !q.isEmpty()) {
            urlBuilder.append("&q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
        }
        if (category != null && !category.isEmpty()) {
            urlBuilder.append("&category=").append(category);
        }
        if (country != null && !country.isEmpty()) {
            urlBuilder.append("&country=").append(country);
        }
        if (language != null && !language.isEmpty()) {
            urlBuilder.append("&language=").append(language);
        }
        if (sortBy != null && !sortBy.isEmpty()) {
            urlBuilder.append("&sortBy=").append(sortBy);
        }
        if (pageSize > 0) {
            urlBuilder.append("&pageSize=").append(pageSize);
        }
        if (page > 0) {
            urlBuilder.append("&page=").append(page);
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("X-Api-Key",API_KEY)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        List<NewsArticle> articles = new ArrayList<>();
        if (json.has("articles") && json.get("articles").isJsonArray()) {
            JsonArray articlesArray = json.getAsJsonArray("articles");
            for (JsonElement elem : articlesArray) {
                JsonObject obj = elem.getAsJsonObject();
                String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "";
                String description = obj.has("description") && !obj.get("description").isJsonNull() ? obj.get("description").getAsString() : "";
                String url = obj.has("url") && !obj.get("url").isJsonNull() ? obj.get("url").getAsString() : "";
                String urlToImage = obj.has("urlToImage") && !obj.get("urlToImage").isJsonNull() ? obj.get("urlToImage").getAsString() : "";
                String publishedAt = obj.has("publishedAt") && !obj.get("publishedAt").isJsonNull() ? obj.get("publishedAt").getAsString() : "";
                String sourceName = obj.has("source") && obj.get("source").isJsonObject() && obj.getAsJsonObject("source").has("name") && !obj.getAsJsonObject("source").get("name").isJsonNull() ? obj.getAsJsonObject("source").get("name").getAsString() : "";
                String content = obj.has("content") && !obj.get("content").isJsonNull() ? obj.get("content").getAsString() : "";
                String author = obj.has("author") && !obj.get("author").isJsonNull() ? obj.get("author").getAsString() : "";
                articles.add(new NewsArticle(title, description, url, urlToImage, publishedAt, sourceName, content, author));
            }
        }
        System.out.println("Articles found: " + articles.size());
        return articles;
    }

    public List<NewsArticle> searchNews(String query, String language, String sortBy, int pageSize, int page) throws Exception {
        StringBuilder urlBuilder = new StringBuilder("https://newsapi.org/v2/everything");
        urlBuilder.append("?apiKey=").append(API_KEY);
        if (query != null && !query.isEmpty()) {
            urlBuilder.append("&q=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
        }
        if (language != null && !language.isEmpty()) {
            urlBuilder.append("&language=").append(language);
        }
        if (sortBy != null && !sortBy.isEmpty()) {
            urlBuilder.append("&sortBy=").append(sortBy);
        }
        if (pageSize > 0) {
            urlBuilder.append("&pageSize=").append(pageSize);
        }
        if (page > 0) {
            urlBuilder.append("&page=").append(page);
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("X-Api-Key", API_KEY)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        List<NewsArticle> articles = new ArrayList<>();
        if (json.has("articles") && json.get("articles").isJsonArray()) {
            JsonArray articlesArray = json.getAsJsonArray("articles");
            for (JsonElement elem : articlesArray) {
                JsonObject obj = elem.getAsJsonObject();
                String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "";
                String description = obj.has("description") && !obj.get("description").isJsonNull() ? obj.get("description").getAsString() : "";
                String url = obj.has("url") && !obj.get("url").isJsonNull() ? obj.get("url").getAsString() : "";
                String urlToImage = obj.has("urlToImage") && !obj.get("urlToImage").isJsonNull() ? obj.get("urlToImage").getAsString() : "";
                String publishedAt = obj.has("publishedAt") && !obj.get("publishedAt").isJsonNull() ? obj.get("publishedAt").getAsString() : "";
                String sourceName = obj.has("source") && obj.get("source").isJsonObject() && obj.getAsJsonObject("source").has("name") && !obj.getAsJsonObject("source").get("name").isJsonNull() ? obj.getAsJsonObject("source").get("name").getAsString() : "";
                String content = obj.has("content") && !obj.get("content").isJsonNull() ? obj.get("content").getAsString() : "";
                String author = obj.has("author") && !obj.get("author").isJsonNull() ? obj.get("author").getAsString() : "";
                articles.add(new NewsArticle(title, description, url, urlToImage, publishedAt, sourceName, content, author));
            }
        }
        System.out.println("Search found: " + articles.size() + " articles.");
        return articles;
    }
}
