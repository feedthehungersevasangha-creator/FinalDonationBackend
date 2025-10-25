package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "declaration")
public class Declaration {
    @Id
    private String id;
    private String title;
    private String content;

    public Declaration() {}

    public Declaration(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}
