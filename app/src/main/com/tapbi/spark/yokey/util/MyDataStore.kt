package com.tapbi.spark.yokey.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.tapbi.spark.yokey.common.Constant
import kotlinx.coroutines.flow.Flow


import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("pref")

class MyDataStore(private val context: Context) {
    companion object {
        /**
         * init datastore
         */
        //  private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("MyDataStore")
        var RATE_APP = stringPreferencesKey("Constant.HIDE_RATE_APP")
        val AGE = intPreferencesKey("AGE")
        val VALUE_BOOLEAN = booleanPreferencesKey(Constant.HIDE_RATE_APP)
    }

    /**
     * Write value to a Preferences DataStore
     */
    suspend fun putString(name: String, key: Preferences.Key<String>) {
        context.dataStore.edit {
            it[key] = name
        }
    }

    suspend fun putDemo(name: String, age: Int, key: Preferences.Key<String>) {
        RATE_APP = key
        context.dataStore.edit {
            it[RATE_APP] = name
            it[AGE] = age
        }
    }

    suspend fun putInt(value: Int, key: Preferences.Key<Int>) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    suspend fun putInt(value: Int, key: String) {
        context.dataStore.edit {
            it[intPreferencesKey(key)] = value
        }
    }

    suspend fun putBoolean(value: Boolean) {
        context.dataStore.edit {
            it[VALUE_BOOLEAN] = value
        }
    }

    suspend fun putBoolean(value: Boolean, key: String) {
        context.dataStore.edit {
            it[booleanPreferencesKey(key)] = value
        }
    }

    suspend fun clearDataKey() {
        context.dataStore.edit { preference ->
            preference.remove(VALUE_BOOLEAN)
        }
    }

    /**
     * Get value from a Preferences DataStore
     */
    fun getString(key: Preferences.Key<String>): Flow<String> = context.dataStore.data.map {
        it[key] ?: "null"
    }

    fun getInt(key: Preferences.Key<Int>): Flow<Int> = context.dataStore.data.map {
        it[key] ?: -1
    }

    fun getInt(key: String): Flow<Int> = context.dataStore.data.map {
        it[intPreferencesKey(key)] ?: -1
    }

    fun getBoolean(): Flow<Boolean> = context.dataStore.data.map {
        it[VALUE_BOOLEAN] ?: false
    }

    fun getBoolean(key: String): Flow<Boolean> = context.dataStore.data.map {
        it[VALUE_BOOLEAN] ?: false
    }

    fun getDemo() = context.dataStore.data.map {
        it[RATE_APP] ?: ""
    }

    fun getDemoAge() = context.dataStore.data.map {
        it[AGE] ?: -1
    }
}