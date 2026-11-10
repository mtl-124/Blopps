package com.lajara.blopps.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PublicacionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun publicacionDao(): PublicacionDao

    companion object {
        fun crear(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "blopps.db").build()
        }
    }
}