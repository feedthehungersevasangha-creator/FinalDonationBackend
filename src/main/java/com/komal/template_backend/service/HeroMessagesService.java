package com.komal.template_backend.service;


import com.komal.template_backend.model.HeroMessages;
import com.komal.template_backend.model.MessageItem;
import com.komal.template_backend.repo.HeroMessagesRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class HeroMessagesService {

    private final HeroMessagesRepository repo;

    public HeroMessagesService(HeroMessagesRepository repo) {
        this.repo = repo;
    }

    public HeroMessages getMessages() {
        return repo.findAll().stream().findFirst().orElseGet(() -> {
            HeroMessages defaultDoc = new HeroMessages();
            defaultDoc.setHeroMessages(Arrays.asList(
                    new MessageItem("Share a Meal"),
                    new MessageItem("Share a Smile"),
                    new MessageItem("Join Hands to End Hunger")
            ));
            return repo.save(defaultDoc);
        });
    }

    public HeroMessages updateMessages(HeroMessages updated) {
        HeroMessages existing = getMessages();
        existing.setHeroMessages(updated.getHeroMessages());
        return repo.save(existing);
    }
}