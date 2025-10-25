package com.komal.template_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Setter
@Getter
@Document(collection = "publications")
public class Publication {

    // Getters and Setters
    @Id
    private String id;

    private String title;
    private String description;
    private String imageUrl;
    private String summary;
    private String content;
    // Constructors
    public Publication() {}

    public Publication(String title, String description, String imageUrl,String summary,String content) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.summary=summary;
        this.content=content;

    }

}
