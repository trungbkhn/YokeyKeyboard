package com.tapbi.spark.yokey.data.model;

import com.google.gson.annotations.SerializedName;

public class PaginationTheme {

    @SerializedName("id_theme")
    private int idTheme;
    @SerializedName("id_category")
    private int idCategory;
    @SerializedName("is_hot")
    private int isHot;
    @SerializedName("sort_key")
    private int sortKey;

    @SerializedName("last_version")
    private int lastVersion;

    public PaginationTheme(int idTheme, int idCategory, int isHot, int sortKey) {
        this.idTheme = idTheme;
        this.idCategory = idCategory;
        this.isHot = isHot;
        this.sortKey = sortKey;
    }

    public PaginationTheme(int idTheme, int idCategory, int isHot, int sortKey, int lastVersion) {
        this.idTheme = idTheme;
        this.idCategory = idCategory;
        this.isHot = isHot;
        this.sortKey = sortKey;
        this.lastVersion = lastVersion;
    }

    public int getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(int lastVersion) {
        this.lastVersion = lastVersion;
    }

    public int getIdTheme() {
        return idTheme;
    }

    public void setIdTheme(int idTheme) {
        this.idTheme = idTheme;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public int getIsHot() {
        return isHot;
    }

    public void setIsHot(int isHot) {
        this.isHot = isHot;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }
}
