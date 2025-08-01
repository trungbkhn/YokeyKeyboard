package com.tapbi.spark.yokey.data.local.dao

import androidx.room.*
import com.tapbi.spark.yokey.data.local.entity.Symbols

@Dao
interface SymbolsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSymbols(symbols: Symbols)

    @Query("SELECT * FROM SymbolsTable WHERE id=:idSymbols")
    fun fetchOneSymbolsByIdSymbols(idSymbols: Int): Symbols?

    @Query("SELECT * FROM SymbolsTable")
    fun fetchAllSymbols(): List<Symbols>

    @Update
    fun updateSymbols(symbols: Symbols)
    @Query("SELECT EXISTS(SELECT * FROM SymbolsTable WHERE contentSymbols = :contentSymbols)")
    fun isItemIsExist(contentSymbols : String) : Boolean
    @Query("DELETE FROM SymbolsTable WHERE contentSymbols=:contentSymbols")
    fun deleteItem(contentSymbols : String)
    @Query("DELETE FROM SymbolsTable WHERE id NOT IN (SELECT MIN(id) FROM SymbolsTable GROUP BY contentSymbols)")
    fun deleteDuplicates()


}