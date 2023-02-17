package com.example.mongo.repository

import com.example.util.Diaries
import com.example.util.model.Diary
import com.example.util.model.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime


internal interface MongoRepository {

    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
    fun getSelectedId(diaryId: ObjectId): Flow<RequestState<Diary>>
    suspend fun insertDiary(diary: Diary): RequestState<Diary>
    suspend fun updateDiary(diary: Diary): RequestState<Diary>
    suspend fun deleteDiary(id:ObjectId): RequestState<Diary>
    suspend fun deleteAllDiaries(): RequestState<Boolean>
    fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<Diaries>
}