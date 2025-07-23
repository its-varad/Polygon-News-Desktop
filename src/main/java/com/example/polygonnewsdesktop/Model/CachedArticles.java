package com.example.polygonnewsdesktop.Model;

import java.util.List;

public class CachedArticles {
    private List<NewsArticle> articles;
    private long timestamp;

    public CachedArticles(List<NewsArticle> articles, long timestamp) {
        this.articles = articles;
        this.timestamp = timestamp;
    }

    public List<NewsArticle> getArticles() {
        return articles;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setArticles(List<NewsArticle> articles) {
        this.articles = articles;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 