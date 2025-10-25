package com.komal.template_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "press_releases")
public class PressRelease {
    @Id
    private String id;

    private String title;
    private String excerpt;
    private String content;
    private String date;
    private String imageUrl;
    private String summary;

    // getters and setters
}