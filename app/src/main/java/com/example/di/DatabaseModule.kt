package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.diaryapp.data.database.ImageDatabase
import com.example.diaryapp.util.Constants.IMAGE_DATABASE
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
        @ApplicationContext context:Context
    ): ImageDatabase{
        return Room.databaseBuilder(
            context = context,
            name =IMAGE_DATABASE,
            klass = ImageDatabase::class.java
        ).build()
    }

    @Singleton
    @Provides
    fun provideFirstDao(database: ImageDatabase) = database.imageToUploadDao()

    @Singleton
    @Provides
    fun provideImageDeleteDao(database: ImageDatabase) = database.imageToDeleteDao()


}