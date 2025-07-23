package com.example.polygonnewsdesktop.Model;

public class NewsArticle {
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String author;
    private String sourceName;

    private String content;

    public NewsArticle(String title,String description,String url, String urlToImage,
                       String publishedAt,String source,String content,String author) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.sourceName = source;
        this.content = content;
        this.author = author;
    }

    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getUrl() {return url;}
    public String getUrlToImage() {return urlToImage;}
    public String getPublishedAt() {return publishedAt;}
    public String getSourceName() {return sourceName;}
    public String getContent() {return content;}
    public String getAuthor() {return author;}
}
