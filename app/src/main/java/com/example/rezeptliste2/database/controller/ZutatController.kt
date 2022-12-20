package com.example.rezeptliste2.database.controller

import android.content.Context
import androidx.room.Room
import com.example.rezeptliste2.database.Database
import com.example.rezeptliste2.database.dao.RezeptDao
import com.example.rezeptliste2.database.dao.RezeptZutatDao
import com.example.rezeptliste2.database.dao.ZutatDao

class ZutatController(context: Context) {
    private var zutatDao: ZutatDao
    private var rezeptZutatDao: RezeptZutatDao
    private var rezeptDao: RezeptDao
    private var db: Database

    init {
        this.db = Room.databaseBuilder(
            context,
            Database::class.java, "Rezeptliste"
        ).createFromAsset("Database/Rezeptliste.db").build()
        rezeptDao = db.rezeptDao()
        zutatDao = db.zutatDao()
        rezeptZutatDao = db.rezeptZutatDao()
    }
}