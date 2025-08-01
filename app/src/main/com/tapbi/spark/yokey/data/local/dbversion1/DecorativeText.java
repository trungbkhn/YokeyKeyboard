package com.tapbi.spark.yokey.data.local.dbversion1;


public class DecorativeText {
    private String character;
    private boolean isPremium;

    public DecorativeText(String character, boolean isPremium) {
        this.character = character;
        this.isPremium = isPremium;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }




    public DecorativeText() {

    }
}
