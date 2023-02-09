package com.example.diaryapp.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.MongoDb
import com.example.diaryapp.model.Diary
import com.example.diaryapp.util.Diaries
import com.example.diaryapp.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeViewModel : ViewModel(){

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries(){
        viewModelScope.launch {
            MongoDb.getAllDiaries().collect(){result->
                diaries.value = result
            }
        }
    }
}