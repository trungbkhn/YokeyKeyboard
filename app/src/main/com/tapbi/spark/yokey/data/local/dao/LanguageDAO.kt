package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.LanguageEntity

@Dao
interface LanguageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(languageEntities: LanguageEntity?)

    @Update
    fun update(vararg languageEntities: LanguageEntity?)
    @Query("UPDATE lang_db SET is_enabled = :isEnabled WHERE id=:id")
    fun updateLanguage(isEnabled: Boolean, id: Int)

    @Delete
    fun delete(vararg languageEntities: LanguageEntity?)

    @Query("DELETE FROM lang_db")
    fun deleteAllData()
    @Query("DELETE FROM lang_db WHERE locale = :locale")
    fun deleteKeyboardLanguageByLocale(locale : String)


    @get:Query("Select * FROM lang_db")
    val getAllLanguages : List<LanguageEntity?>?

}