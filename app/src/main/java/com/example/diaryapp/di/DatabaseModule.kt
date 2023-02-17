package com.example.diaryapp.di

import android.content.Context
import androidx.room.Room
import com.example.mongo.database.ImageDatabase
import com.example.mongo.database.ImageDatabase.Companion.migration_1_2
import com.example.util.Constants.IMAGE_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ImageDatabase {
        return Room.databaseBuilder(
             context,
            ImageDatabase::class.java,
            IMAGE_DATABASE
        ).addMigrations(migration_1_2).build()
    }

/*    companion object {
        val migration_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS images_to_delete_table (id INTEGER NOT NULL PRIMARY KEY, remoteImagePath TEXT)"
                )
            }
        }
    }*/


    @Singleton
    @Provides
    fun provideFirstDao(database: ImageDatabase) = database.imageToUploadDao()

    @Singleton
    @Provides
    fun provideImageDeleteDao(database: ImageDatabase) = database.imageToDeleteDao()




}
