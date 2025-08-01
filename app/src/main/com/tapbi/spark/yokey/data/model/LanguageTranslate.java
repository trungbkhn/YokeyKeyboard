package com.tapbi.spark.yokey.data.model;

import com.tapbi.spark.yokey.R;

public class LanguageTranslate {

    private String codeLanguage = "";
    private String nameLanguage = "";
    private int codeResource = R.string.langauge_detection;
    private String languageA = "";
    private String languageB = "";
    private String languageC = "";

    public LanguageTranslate(String codeLanguage, String nameLanguage) {
        this.codeLanguage = codeLanguage;
        this.nameLanguage = nameLanguage;
    }

    public int getCodeResource() {
        return codeResource;
    }

    public void setCodeResource(int codeResource) {
        this.codeResource = codeResource;
    }

    public String getLanguageA() {
        return languageA;
    }

    public void setLanguageA(String languageA) {
        this.languageA = languageA;
    }

    public String getLanguageB() {
        return languageB;
    }

    public void setLanguageB(String languageB) {
        this.languageB = languageB;
    }

    public String getLanguageC() {
        return languageC;
    }

    public void setLanguageC(String languageC) {
        this.languageC = languageC;
    }

    public LanguageTranslate(String codeLanguage, String nameLanguage, int codeResource) {
        this.codeLanguage = codeLanguage;
        this.nameLanguage = nameLanguage;
        this.codeResource = codeResource;
    }

    public LanguageTranslate(String codeLanguage, int codeResource, String languageA, String languageB, String languageC) {
        this.codeLanguage = codeLanguage;
        this.codeResource = codeResource;
        this.languageA = languageA;
        this.languageB = languageB;
        this.languageC = languageC;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public String getNameLanguage() {
        return nameLanguage;
    }

    public void setNameLanguage(String nameLanguage) {
        this.nameLanguage = nameLanguage;
    }
}
