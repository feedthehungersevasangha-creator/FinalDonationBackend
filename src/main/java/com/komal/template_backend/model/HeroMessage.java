package com.komal.template_backend.model;

public class HeroMessage {
    private String text;

    public HeroMessage() {}

    public HeroMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
