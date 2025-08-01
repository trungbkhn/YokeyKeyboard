package com.tapbi.spark.yokey.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tapbi.spark.yokey.data.local.LanguageEntity
import com.tapbi.spark.objects.Background
import com.tapbi.spark.yokey.data.local.dao.BackgroundDAO
import com.tapbi.spark.yokey.data.local.dao.EmojiDAO
import com.tapbi.spark.yokey.data.local.dao.ItemFontDAO
import com.tapbi.spark.yokey.data.local.dao.LanguageDAO
import com.tapbi.spark.yokey.data.local.dao.StickerDAO
import com.tapbi.spark.yokey.data.local.dao.StickerRecentDAO
import com.tapbi.spark.yokey.data.local.dao.SymbolsDAO
import com.tapbi.spark.yokey.data.local.dao.ThemeDAO
import com.tapbi.spark.yokey.data.local.entity.Emoji
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.local.entity.StickerRecent
import com.tapbi.spark.yokey.data.local.entity.Symbols
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity

@Database(
    entities = [ThemeEntity::class, ItemFont::class, Sticker::class, Symbols::class, StickerRecent::class, Emoji::class, LanguageEntity::class, Background::class],
    version = ThemeDB.DATABASE_VERSION,
    exportSchema = false
)
abstract class ThemeDB : RoomDatabase() {
    abstract fun themeDAO(): ThemeDAO?
    abstract fun itemFontDAO(): ItemFontDAO?
    abstract fun stickerDAO(): StickerDAO?
    abstract fun symbolsDAO(): SymbolsDAO?
    abstract fun stickerRecentDAO(): StickerRecentDAO?
    abstract fun emojiDAO(): EmojiDAO?
    abstract fun languageDAO(): LanguageDAO?
    abstract fun backgroundDAO() : BackgroundDAO?



    companion object {
        private var themeDB: ThemeDB? = null
        const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "ThemeEntityTable"

        @JvmStatic
        fun getInstance(context: Context?): ThemeDB? {
            if (themeDB == null) {
                themeDB = Room.databaseBuilder(context!!, ThemeDB::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return themeDB
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE ItemFontTable(id INTEGER NOT NULL,textFont TEXT,favorite INTEGER not null,imgBackground TEXT,textDemo TEXT,filterCategories TEXT,isPremium INTEGER not null,isAdd INTEGER not null,dateModify INTEGER not null,PRIMARY KEY('id'))")
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE StickerTable(id_emoji INTEGER not null,name_emoji TEXT,thumb_emoji TEXT,link_emoji TEXT,category_name TEXT,category_emoji TEXT,isDownload INTEGER not null,PRIMARY KEY('id_emoji'))")
                database.execSQL("CREATE TABLE StickerTable(id_sticker INTEGER not null,name_sticker TEXT,thumb_sticker TEXT,link_sticker TEXT,id_category INTEGER not null,category_name TEXT,isDownload INTEGER not null,PRIMARY KEY('id_sticker'))")

            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ThemeEntityTable ADD COLUMN isMyTheme INTEGER NOT NULL DEFAULT " + 0)
                database.execSQL("CREATE TABLE IF NOT EXISTS SymbolsTable(id INTEGER NOT NULL,typeSymbols TEXT,contentSymbols TEXT,timeRecent TEXT NOT NULL DEFAULT '0',PRIMARY KEY('id'))")
                database.execSQL("CREATE TABLE IF NOT EXISTS StickerRecentTable(link TEXT NOT NULL,timeRecent TEXT NOT NULL DEFAULT '0',PRIMARY KEY('link'))")
                database.execSQL("CREATE TABLE IF NOT EXISTS EmojiTable(id INTEGER NOT NULL,content TEXT,title TEXT ,type  INTEGER,favourite  INTEGER ,count_favourite INTEGER  ,PRIMARY KEY('id'))")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5){
            override fun migrate(database: SupportSQLiteDatabase) {
                // todo: create table lang_db
                database.execSQL(
                    "CREATE TABLE lang_db (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL" + ", " +
                            "name TEXT, locale TEXT, " +
                            "extra_values TEXT, subtype_mode TEXT, display_name TEXT, prefer_subtype TEXT, " +
                            "icon_res INTEGER NOT NULL DEFAULT 0, name_res INTEGER  NOT NULL DEFAULT 0, " +
                            "subtype_id INTEGER NOT NULL DEFAULT 0, is_auxiliary INTEGER NOT NULL DEFAULT 0, " +
                            "is_ascii INTEGER NOT NULL DEFAULT 0, is_enabled INTEGER NOT NULL DEFAULT 0, subtype_tag TEXT, " +
                            "override_enable INTEGER NOT NULL DEFAULT 0)"
                )
            }

        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6){
            override fun migrate(database: SupportSQLiteDatabase) {
                // todo: create table background
//                database.execSQL(
//                    "CREATE TABLE BackgroundTable (id_bg INTEGER PRIMARY KEY NOT NULL" + ", " +
//                            "link_bg_thumb TEXT, link_bg TEXT, version_update INTEGER NOT NULL DEFAULT 1)"
//                )

                database.execSQL("CREATE TABLE BackgroundTable(id_bg INTEGER not null,link_bg_thumb TEXT not null,link_bg TEXT not null,version_update INTEGER not null,PRIMARY KEY('id_bg'))")

            }

        }
    }
}