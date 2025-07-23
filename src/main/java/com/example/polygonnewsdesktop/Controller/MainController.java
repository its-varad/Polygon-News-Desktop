package com.example.polygonnewsdesktop.Controller;

import com.example.polygonnewsdesktop.Model.NewsArticle;
import com.example.polygonnewsdesktop.Model.CachedArticles;
import com.example.polygonnewsdesktop.Service.NewsApiService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import javafx.scene.layout.HBox;

public class MainController {
    @FXML private Button homeButton;
    @FXML private Button businessButton;
    @FXML private Button worldButton;
    @FXML private Button healthButton;
    @FXML private Button sportsButton;
    @FXML private VBox newsContainer;
    @FXML private Button refreshButton;
    @FXML private HBox floatingSidebar;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;

    private final NewsApiService newsApiService = new NewsApiService();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Task<List<NewsArticle>> currentTask;
    private final Map<String, CachedArticles> articleCache = new ConcurrentHashMap<>();
    private static final String CACHE_FILE = System.getProperty("user.home") + File.separator + ".polygon_news_cache.json";
    private static final long CACHE_EXPIRY_MS = 12 * 60 * 60 * 1000L; // 12 hours
    private final Gson gson = new Gson();
    private String currentCategory = "home";

    private double sidebarDragOffsetX = 0;
    private double sidebarDragOffsetY = 0;
    private boolean isGlobalSearch = false;
    private String lastSearchQuery = "";

    @FXML
    private void initialize() {
        loadCacheFromDisk();
        homeButton.setOnAction(e -> onCategorySelected("home"));
        businessButton.setOnAction(e -> onCategorySelected("business"));
        worldButton.setOnAction(e -> onCategorySelected("world"));
        healthButton.setOnAction(e -> onCategorySelected("health"));
        sportsButton.setOnAction(e -> onCategorySelected("sports"));
        refreshButton.setOnAction(e -> refreshCurrentCategory());

        // Search bar logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearSearchButton.setVisible(!newVal.isEmpty());
        });
        searchField.setOnAction(e -> onSearch());
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            isGlobalSearch = false;
            lastSearchQuery = "";
            loadNews(currentCategory);
        });

        loadNews("home");
    }

    private void onCategorySelected(String category) {
        currentCategory = category;
        loadNews(category);
        System.out.println("Selected category: " + category);
    }

    private void refreshCurrentCategory() {
        articleCache.remove(currentCategory);
        loadNews(currentCategory);
    }

    private List<NewsArticle> fetchWithFallback(String category, String country) throws Exception {
        List<NewsArticle> articles = newsApiService.fetchNews(null, category, country, "en", "publishedAt", 20, 1);
        if (articles.isEmpty() && !country.equals("us")) {
            System.out.println("No articles found for " + country + ", falling back to US.");
            articles = newsApiService.fetchNews(null, category, "us", "en", "publishedAt", 20, 1);
            if (!articles.isEmpty()) {
                // Add a marker article to indicate fallback (optional, for UI message)
                NewsArticle fallbackNotice = new NewsArticle(
                    "Showing US news instead.",
                    "No articles found for this category in India. Displaying US news as fallback.",
                    "", "", "", "", "", ""
                );
                articles.add(0, fallbackNotice);
            }
        }
        return articles;
    }

    private void loadNews(String category) {
        // Cancel previous task if running
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }

        newsContainer.getChildren().clear();
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        newsContainer.getChildren().add(progress);

        // Check cache first
        CachedArticles cached = articleCache.get(category);
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.getTimestamp() < CACHE_EXPIRY_MS)) {
            System.out.println("Loading articles for '" + category + "' from persistent cache.");
            displayArticles(cached.getArticles());
            return;
        }

        currentTask = new Task<>() {
            @Override
            protected List<NewsArticle> call() throws Exception {
                System.out.println("Fetching news in background thread...");
                List<NewsArticle> articles;
                try {
                    switch(category) {
                        case "business":
                        case "health":
                        case "sports":
                            articles = fetchWithFallback(category, "in");
                            break;
                        case "home":
                            articles = fetchWithFallback(null, "in");
                            break;
                        case "world":
                            articles = newsApiService.fetchNews(null, "general", "us", "en", "publishedAt", 20, 1);
                            break;
                        default:
                            articles = fetchWithFallback(null, "in");
                            break;
                    }
                    System.out.println("Fetched " + articles.size() + " articles.");
                    // Store in cache and save to disk
                    articleCache.put(category, new CachedArticles(articles, System.currentTimeMillis()));
                    saveCacheToDisk();
                    return articles;
                } catch (Exception ex) {
                    // On API failure, use old cache if available
                    if (cached != null) {
                        System.out.println("API fetch failed, using old cache for '" + category + "'.");
                        return cached.getArticles();
                    } else {
                        throw ex;
                    }
                }
            }
        };

        currentTask.setOnSucceeded(e -> {
            displayArticles(currentTask.getValue());
        });

        currentTask.setOnFailed(e -> {
            newsContainer.getChildren().clear();
            newsContainer.getChildren().add(new Label("Error loading news: " + currentTask.getException().getMessage()));
        });

        executor.execute(currentTask);
    }

    private void displayArticles(List<NewsArticle> articles) {
        System.out.println("displayArticles called with " + articles.size() + " articles.");
        newsContainer.getChildren().clear();

        if (articles.isEmpty()) {
            newsContainer.getChildren().add(new Label("No news articles found."));
            return;
        }

        // If the first article is a fallback notice, show it as a message and skip rendering it as a card
        if (articles.get(0).getTitle().equals("Showing US news instead.")) {
            Label fallbackLabel = new Label(articles.get(0).getDescription());
            fallbackLabel.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-text-fill: #856404;");
            newsContainer.getChildren().add(fallbackLabel);
            articles = articles.subList(1, articles.size());
        }

        // Sort articles by publishedAt descending (newest first)
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        articles.sort(Comparator.comparing((NewsArticle a) -> {
            try {
                return ZonedDateTime.parse(a.getPublishedAt(), formatter);
            } catch (Exception e) {
                return ZonedDateTime.parse("1970-01-01T00:00:00Z", formatter);
            }
        }).reversed());

        // Create skeleton cards first
        for (NewsArticle article : articles) {
            VBox card = createArticleCardSkeleton(article);
            newsContainer.getChildren().add(card);
        }

        // Load images asynchronously
        for (int i = 0; i < articles.size(); i++) {
            NewsArticle article = articles.get(i);
            VBox card = (VBox) newsContainer.getChildren().get(i + (newsContainer.getChildren().size() > articles.size() ? 1 : 0));
            loadArticleImageAsync(article, card);
        }
    }

    private VBox createArticleCardSkeleton(NewsArticle article) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #ccc, 4, 0, 0, 2);");

        // Image placeholder
        Pane imagePlaceholder = new Pane();
        imagePlaceholder.setPrefSize(320, 180);
        imagePlaceholder.setStyle("-fx-background-color: #f0f0f0;");
        card.getChildren().add(imagePlaceholder);

        // Title
        Label title = new Label(article.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(320);
        card.getChildren().add(title);

        // Description
        if (article.getDescription() != null && !article.getDescription().isEmpty()) {
            Text desc = new Text(article.getDescription());
            desc.setWrappingWidth(320);
            card.getChildren().add(desc);
        }

        // Source and date (formatted)
        String formattedDate = article.getPublishedAt();
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            formattedDate = ZonedDateTime.parse(article.getPublishedAt(), inputFormatter).format(outputFormatter);
        } catch (Exception e) {
            // fallback to original string
        }
        Label meta = new Label("Source: " + article.getSourceName() + " | " + formattedDate);
        meta.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        card.getChildren().add(meta);

        // Read More button
        Button readMore = new Button("Read More");
        readMore.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(article.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        card.getChildren().add(readMore);

        return card;
    }

    private void loadArticleImageAsync(NewsArticle article, VBox card) {
        if (article.getUrlToImage() == null || article.getUrlToImage().isEmpty()) {
            return;
        }

        executor.execute(() -> {
            try {
                System.out.println("Loading image: " + article.getUrlToImage());
                Image image = new Image(article.getUrlToImage(), 320, 180, true, true, true);

                if (!image.isError()) {
                    Platform.runLater(() -> {
                        ImageView imageView = new ImageView(image);
                        // Replace placeholder with actual image
                        card.getChildren().set(0, imageView);
                    });
                } else {
                    System.out.println("Image loading error: " + article.getUrlToImage());
                }
            } catch (Exception e) {
                System.out.println("Image load failed for: " + article.getUrlToImage());
            }
        });
    }

    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            isGlobalSearch = false;
            lastSearchQuery = "";
            loadNews(currentCategory);
            return;
        }
        isGlobalSearch = true;
        lastSearchQuery = query;
        performGlobalSearch(query);
    }

    private void performGlobalSearch(String query) {
        newsContainer.getChildren().clear();
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        newsContainer.getChildren().add(progress);

        Task<List<NewsArticle>> searchTask = new Task<>() {
            @Override
            protected List<NewsArticle> call() throws Exception {
                return newsApiService.searchNews(query, "en", "publishedAt", 20, 1);
            }
        };
        searchTask.setOnSucceeded(e -> displayArticles(searchTask.getValue()));
        searchTask.setOnFailed(e -> {
            newsContainer.getChildren().clear();
            newsContainer.getChildren().add(new Label("Error loading search results: " + searchTask.getException().getMessage()));
        });
        executor.execute(searchTask);
    }

    private void loadCacheFromDisk() {
        try {
            Path path = Paths.get(CACHE_FILE);
            if (Files.exists(path)) {
                String json = Files.readString(path);
                Type type = new TypeToken<HashMap<String, CachedArticles>>(){}.getType();
                Map<String, CachedArticles> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    articleCache.clear();
                    articleCache.putAll(loaded);
                }
                System.out.println("Loaded article cache from disk.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load cache from disk: " + e.getMessage());
        }
    }

    private void saveCacheToDisk() {
        try {
            String json = gson.toJson(articleCache);
            Files.writeString(Paths.get(CACHE_FILE), json);
            System.out.println("Saved article cache to disk.");
        } catch (Exception e) {
            System.out.println("Failed to save cache to disk: " + e.getMessage());
        }
    }
}