package com.example.diaryapp.data.repository

import com.example.diaryapp.model.Diary
import com.example.diaryapp.util.Diaries
import com.example.diaryapp.util.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow


interface MongoRepository {

    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>
    fun getSelectedId(diaryId: ObjectId): Flow<RequestState<Diary>>
    suspend fun insertDiary(diary: Diary): RequestState<Diary>
    suspend fun updateDiary(diary: Diary): RequestState<Diary>
    suspend fun deleteDiary(id:ObjectId): RequestState<Diary>
}