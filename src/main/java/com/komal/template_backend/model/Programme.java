package com.komal.template_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "Programmes")
public class Programme {
    @Id
    private String _id;
    private String title;
    private String description;
    private String icon;
    private String color;


}
