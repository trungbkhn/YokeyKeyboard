package com.tapbi.spark.yokey.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tapbi.spark.objects.Background

@Dao
interface BackgroundDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackground(background: Background?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackgroundList(itemBackgroundList: List<Background>?)

    @get:Query("SELECT * FROM BackgroundTable")
    val allBackground: List<Background>?


}