package com.example.diaryapp.data.repository

import com.example.diaryapp.model.Diary
import com.example.diaryapp.util.Diaries
import com.example.diaryapp.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


interface MongoRepository {

    fun configureTheRealm()
    fun getAllDiaries(): Flow<Diaries>


}