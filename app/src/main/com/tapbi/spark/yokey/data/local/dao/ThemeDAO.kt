package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity

@Dao
interface ThemeDAO {
    @Insert
    fun insertTheme(themeEntity: ThemeEntity?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(themeEntities : ArrayList<ThemeEntity>)

    @Query("SELECT * FROM ThemeEntityTable WHERE id=:idTheme")
    fun fetchOneThemeByIdTheme(idTheme: String?): ThemeEntity?

    @Query("SELECT * FROM ThemeEntityTable WHERE isMyTheme=:isMyTheme")
    fun fetchListThemeByIsMyTheme(isMyTheme: Int): MutableList<ThemeEntity>?

    @Query("SELECT * FROM ThemeEntityTable WHERE isHotTheme=:isHotTheme")
    fun fetchListThemeByIsHotTheme(isHotTheme : Int): MutableList<ThemeEntity>?

    @Query("SELECT * FROM ThemeEntityTable WHERE typeKeyboard=:typeKeyboard AND isMyTheme=0")
    fun fetchListThemeByTypeKeyboard(typeKeyboard : String): MutableList<ThemeEntity>?

    @Query("SELECT * FROM ThemeEntityTable WHERE isTopTheme =:idCategory AND isMyTheme=0")
    fun fetchListThemeByIdCategory(idCategory : String): MutableList<ThemeEntity>?

    @Delete
    fun deleteTheme(themeEntity: ThemeEntity?)

    @Update
    fun updateTheme(themeEntity: ThemeEntity?)

    @Query("DELETE FROM ThemeEntityTable WHERE id = :id")
    fun deleteThemeById(id: String)
}