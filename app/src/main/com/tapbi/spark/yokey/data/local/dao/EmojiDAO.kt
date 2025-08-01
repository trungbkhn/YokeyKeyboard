package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.entity.Emoji

@Dao
interface EmojiDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEmoji(emoji: Emoji?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEmojiList(itemEmojiList: List<Emoji?>?)

    @Query("SELECT * FROM EmojiTable WHERE id=:id")
    fun fetchOneEmojiByIdEmoji(id: Int): Emoji?

    @get:Query("SELECT * FROM EmojiTable")
    val allEmoji: List<Emoji?>?

    @Query("SELECT * FROM EmojiTable WHERE type=:type")
    fun allEmojiByType(type: Int): List<Emoji?>?

    @Query("SELECT * FROM EmojiTable WHERE favourite=:favourite")
    fun allEmojiByFavourite(favourite: Int): List<Emoji?>?

    @Delete
    fun deleteEmoji(itemEmoji: Emoji?)

    @Update
    fun updateEmoji(itemEmoji: Emoji?)

    @Query("UPDATE EmojiTable SET favourite = :favourite WHERE content=:content")
    fun updateFavourite(favourite: Int, content: String)

    @Query("UPDATE EmojiTable SET title = :title WHERE content=:content")
    fun updateContent(title: String, content: String)

    @Query("SELECT * FROM EmojiTable WHERE content=:content")
    fun getEmojiByContent(content: String): List<Emoji?>?
}