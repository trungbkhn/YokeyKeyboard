package com.tapbi.spark.yokey.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "lang_db")
public class LanguageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo (name = "display_name")
    public String displayName;

    @ColumnInfo (name = "locale")
    public String locale;

    @ColumnInfo (name = "extra_values")
    public String extraValues;

    @ColumnInfo (name = "is_ascii")
    public boolean isAscii;

    @ColumnInfo (name = "is_enabled")
    public boolean isEnabled;

    @ColumnInfo (name = "is_auxiliary")
    public boolean isAuxiliary;

    @ColumnInfo (name = "icon_res")
    public int iconRes;

    @ColumnInfo (name = "name_res")
    public int nameRes;

    @ColumnInfo (name = "subtype_id")
    public int subtypeId;

    @ColumnInfo (name = "subtype_tag")
    public String subtypeTag;

    @ColumnInfo (name = "subtype_mode")
    public String subtypeMode;

    @ColumnInfo (name = "override_enable")
    public boolean overrideEnable;

    @ColumnInfo (name = "prefer_subtype")
    public String prefSubtype;
    @Ignore
    public int indexList;
    @Override
    public boolean equals(Object o) {
        LanguageEntity that = (LanguageEntity) o;
        return id == that.id &&
                isAscii == that.isAscii &&
                isEnabled == that.isEnabled &&
                isAuxiliary == that.isAuxiliary &&
                iconRes == that.iconRes &&
                nameRes == that.nameRes &&
                subtypeId == that.subtypeId &&
                overrideEnable == that.overrideEnable &&
                Objects.equals(name, that.name) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(locale, that.locale) &&
                Objects.equals(extraValues, that.extraValues) &&
                Objects.equals(subtypeTag, that.subtypeTag) &&
                Objects.equals(subtypeMode, that.subtypeMode) &&
                Objects.equals(prefSubtype, that.prefSubtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, displayName, locale, extraValues, isAscii, isEnabled, isAuxiliary, iconRes, nameRes, subtypeId, subtypeTag,
                subtypeMode, overrideEnable, prefSubtype);
    }
}