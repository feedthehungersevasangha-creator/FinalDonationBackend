package com.komal.template_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "hero")
public class Hero {

    @Id
    private String id;

    private String title;
    private String subtitle;
    private String backgroundImage;
    private String textColor;

    private String mealText;
    private String mealIcon;

    private String smileText;
    private String smileIcon;

    private String handsText;
    private String handsIcon;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getMealText() { return mealText; }
    public void setMealText(String mealText) { this.mealText = mealText; }

    public String getMealIcon() { return mealIcon; }
    public void setMealIcon(String mealIcon) { this.mealIcon = mealIcon; }

    public String getSmileText() { return smileText; }
    public void setSmileText(String smileText) { this.smileText = smileText; }

    public String getSmileIcon() { return smileIcon; }
    public void setSmileIcon(String smileIcon) { this.smileIcon = smileIcon; }

    public String getHandsText() { return handsText; }
    public void setHandsText(String handsText) { this.handsText = handsText; }

    public String getHandsIcon() { return handsIcon; }
    public void setHandsIcon(String handsIcon) { this.handsIcon = handsIcon; }
}
