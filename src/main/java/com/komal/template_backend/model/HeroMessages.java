package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "hero_messages")
public class HeroMessages {

    @Id
    private String id;

    private List<MessageItem> heroMessages;

    // getters + setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<MessageItem> getHeroMessages() { return heroMessages; }
    public void setHeroMessages(List<MessageItem> heroMessages) { this.heroMessages = heroMessages; }
}