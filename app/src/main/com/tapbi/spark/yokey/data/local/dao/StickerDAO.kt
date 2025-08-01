package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.entity.Sticker

@Dao
interface StickerDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSticker(sticker: Sticker?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickerList(itemStickerList: List<Sticker?>?)

    @Query("SELECT * FROM StickerTable WHERE id_sticker=:id")
    fun fetchOneStickerByIdSticker(id: Int): Sticker?

    @get:Query("SELECT * FROM StickerTable")
    val allSticker: List<Sticker?>?

    @Query("SELECT * FROM StickerTable WHERE isDownload=:isDownload And id_category=:idCategory")
    fun getAllStickerIsDownload(isDownload: Int, idCategory: Int): List<Sticker?>?

    @Query("SELECT * FROM StickerTable WHERE id_category=:idCategory")
    fun getAllStickerByCategory(idCategory: Int): List<Sticker?>?

    @Query("SELECT * FROM StickerTable WHERE isDownload=:isDownload")
    fun getAllStickerIsDownload(isDownload: Int): List<Sticker?>?

    @Delete
    fun deleteSticker(itemSticker: Sticker?)

    @Update
    fun updateSticker(itemSticker: Sticker?)

    @Query("UPDATE StickerTable SET isDownload=:isDownload WHERE id_sticker =:id")
    fun updateIsAdd(id: Int, isDownload: Int)
}