package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.entity.ItemFont

@Dao
interface ItemFontDAO {
    @Insert
    fun insertFont(itemFont: ItemFont?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFontList(itemFontList: List<ItemFont?>?)

    @Query("SELECT * FROM ItemFontTable WHERE id=:id")
    fun fetchOneFontByIdFont(id: Int): ItemFont?

    @get:Query("SELECT * FROM ItemFontTable")
    val allFont: List<ItemFont?>?

    @Query("SELECT * FROM ItemFontTable WHERE textFont=:textFont")
    fun getFontByTextFont(textFont: String?): List<ItemFont?>?

    @Query("SELECT * FROM ItemFontTable WHERE textFont=:textFont")
    fun getFontTextFont(textFont: String?): Boolean
    @Query("DELETE FROM ItemFontTable WHERE textFont=:textFont")
    fun deleteItem(textFont : String)
    @Query("SELECT * FROM ItemFontTable WHERE isAdd=:isAdd")
    fun getAllFontIsAdd(isAdd: Int): List<ItemFont?>?

    @Delete
    fun deleteFont(itemFont: ItemFont?)

    @Update
    fun updateFont(itemFont: ItemFont?)

    @Query("UPDATE ItemFontTable SET isAdd=:isAdd WHERE id =:id")
    fun updateIsAdd(id: Int, isAdd: Int)

    @Query("UPDATE ItemFontTable SET textFont=:textFont WHERE id =:id")
    fun updateTextFont(id: Int, textFont: String?)

    @Query("DELETE FROM ItemFontTable WHERE id NOT IN (SELECT MIN(id) FROM ItemFontTable GROUP BY textFont)")
    fun deleteDuplicates()
}