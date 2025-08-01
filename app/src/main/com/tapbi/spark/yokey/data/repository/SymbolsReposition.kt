package com.tapbi.spark.yokey.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.Observable
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.db.ThemeDB
import com.tapbi.spark.yokey.data.local.dbversion1.DecorativeText
import com.tapbi.spark.yokey.data.local.dbversion1.Emoji
import com.tapbi.spark.yokey.data.local.dbversion1.Version1SqliteOpenHelper
import com.tapbi.spark.yokey.data.local.entity.Symbols
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.objects.Background
import com.tapbi.spark.yokey.util.Constant
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SymbolsReposition(context: Context) {

    lateinit var observer: Observable<List<Emoji>>
    lateinit var version1SqliteOpenHelper: Version1SqliteOpenHelper
    var themDB: ThemeDB

    init {
        themDB = ThemeDB.getInstance(context)!!
        if (!App.instance.mPrefs!!.getBoolean(Constant.CHECK_ADD_DATA_SYMBOLS, false)) {
            version1SqliteOpenHelper = Version1SqliteOpenHelper(context)
            addDataToDB()
        }


    }


    @SuppressLint("CommitPrefEdits")
    private fun addDataToDB() {
        CoroutineScope(IO).launch {
//            withContext(IO) {
            try {
            version1SqliteOpenHelper.createDataBase()
            val listDecorativeText = getDecorativeText()
            val listEmoji = getEmoji()
            version1SqliteOpenHelper.close()
            if (listDecorativeText!!.isNotEmpty() && listEmoji!!.isNotEmpty()) {
                App.instance.mPrefs!!.edit().putBoolean(Constant.CHECK_ADD_DATA_SYMBOLS, true)
                    .apply()
            }
            addDecorativeTextToSymbolsDB(listDecorativeText as ArrayList<DecorativeText>)
            addEmojiToSymbolsDB(listEmoji as ArrayList<Emoji>, listDecorativeText.size)
              }catch (e : NoSuchMethodException){
                  Timber.e("Duongcv " + e.message)
            }
        }
    }

    fun getDecorativeText(): List<DecorativeText>? {
        val list: MutableList<DecorativeText> = ArrayList()
        val sqLiteDatabase: SQLiteDatabase = version1SqliteOpenHelper.readableDatabase
        val get = "SELECT * FROM decoration"
        var cursor : Cursor?=null
        try {
            cursor = sqLiteDatabase.rawQuery(get, arrayOf())
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val r = Random()
                    val i1 = r.nextInt(3)
                    if (i1 == 0) {
                        list.add(DecorativeText(cursor.getString(1), true))
                    } else {
                        list.add(DecorativeText(cursor.getString(1), false))
                    }
                    cursor.moveToNext()
                }
            }
            sqLiteDatabase.close()
        } catch (e: Exception) {

        }finally {
            cursor?.close()
        }
        return list
    }

    fun deleteItemDuplicate() {
        themDB.symbolsDAO()
    }

    fun getEmoji(): List<Emoji>? {
        val emojiList: MutableList<Emoji> = ArrayList()
        val sqLiteDatabase = version1SqliteOpenHelper.readableDatabase
        var cursor : Cursor?=null
        try {
            val get =
                "SELECT cat_name, textemoji FROM  textemoji_category INNER JOIN textemoji_characters ON textemoji_category.cat_id = textemoji_characters.cat_id"
             cursor = sqLiteDatabase.rawQuery(get, arrayOf())
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    emojiList.add(
                        Emoji(
                            cursor.getString(0),
                            cursor.getString(1).trim { it <= ' ' })
                    )
                    cursor.moveToNext()
                }
            }
            sqLiteDatabase.close()
        } catch (e: Exception) {
        }finally {
            cursor?.close()
        }
        return emojiList
    }

    private fun addDecorativeTextToSymbolsDB(listDecorative: ArrayList<DecorativeText>) {
        if (listDecorative.size > 0) {
            for (i in 0 until listDecorative.size) {
                themDB.symbolsDAO()!!.insertSymbols(
                    Symbols(
                        i,
                        Constant.TYPE_SYMBOLS_DECORATIVETEXT,
                        listDecorative[i].character
                    )
                )
            }
        }
    }

    private fun addEmojiToSymbolsDB(listEmoji: ArrayList<Emoji>, pos: Int) {
        if (listEmoji.size > 0) {
            for (i in 0 until listEmoji.size) {
                themDB.symbolsDAO()!!.insertSymbols(
                    Symbols(
                        i + pos,
                        Constant.TYPE_SYMBOLS_EMOJI,
                        listEmoji[i].character
                    )
                )
            }
        }
    }

    fun getAllSymbols() {
        CoroutineScope(IO).launch {
            //  withContext(IO) {
            themDB.symbolsDAO()!!.deleteDuplicates()
            var listSymbols= ArrayList<Symbols>()
            try {
                listSymbols=  themDB.symbolsDAO()!!.fetchAllSymbols() as ArrayList<Symbols>
            }catch (ex :Exception){

            }
            val listSymbolsEmoji = ArrayList<Symbols>()
            val listSymbolsDecorativeText = ArrayList<Symbols>()
            if (listSymbols.size > 0) {
                for (i in 0 until listSymbols.size) {
                    if (listSymbols[i].typeSymbols.equals(Constant.TYPE_SYMBOLS_EMOJI)) {
                        listSymbolsEmoji.add(listSymbols[i])
                        continue
                    } else if (listSymbols[i].typeSymbols.equals(Constant.TYPE_SYMBOLS_DECORATIVETEXT)) {
                        listSymbolsDecorativeText.add(listSymbols[i])
                    }
                }
            }
            val bundle = Bundle()
            bundle.putParcelableArrayList(Constant.DATA_SYMBOLS, listSymbols)
            bundle.putParcelableArrayList(Constant.DATA_SYMBOLS_EMOJI, listSymbolsEmoji)
            bundle.putParcelableArrayList(
                Constant.DATA_SYMBOLS_DECORATIVE,
                listSymbolsDecorativeText
            )
            EventBus.getDefault().postSticky(MessageEvent(Constant.EVENT_DATA_SYMBOLS, bundle))
            // }
        }
    }

    fun updateSymbolsDB(symbols: Symbols) {
        CoroutineScope(IO).launch {
            // withContext(IO) {
            themDB.symbolsDAO()!!.updateSymbols(symbols)
            // }
        }
    }

    fun loadAllImageDownloaded(folder: String): MutableList<Background>? {
        val arrImageEdited = mutableListOf<Background>()
        val file = File(folder)
        val files = file.listFiles() ?: return null
        for (i in files.indices) {
            if (files[i].isFile)
                if (files[i].path.endsWith(".jpg")
                ) {
                    val clean = files[i].path.replace("\\D+".toRegex(), "")
                    val idBg = clean.toLong()
                    arrImageEdited.add(Background(files[i].path, files[i].path, 1, idBg.toInt()))
                }
        }
        return arrImageEdited
    }
}


