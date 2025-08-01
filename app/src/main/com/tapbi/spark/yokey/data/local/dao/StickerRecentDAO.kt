package com.tapbi.spark.yokey.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tapbi.spark.yokey.data.local.entity.StickerRecent

@Dao
interface StickerRecentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickerRecent(stickerRecent: StickerRecent?)

    @get:Query("SELECT * FROM StickerRecentTable")
    val allStickerRecent: List<StickerRecent?>?
}