package com.example.diaryapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class,ImageToDelete::class],
    exportSchema = false,
    version = 2
)
abstract class ImageDatabase: RoomDatabase() {

    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao

}