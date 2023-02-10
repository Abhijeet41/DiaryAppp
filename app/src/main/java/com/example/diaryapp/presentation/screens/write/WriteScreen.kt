@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)

package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import java.time.ZonedDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    onBackPressed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    pagerState: PagerState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    moodName: ()-> String,
    onSavedClick: (Diary) -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit
) {
    //update the Mood when selecting an existing Diary
    LaunchedEffect(key1 = uiState.mood ){
        //.ordinal is used get position from mood
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }

    Scaffold(topBar = {
        WriteTopBar(
            onBackPressed = onBackPressed,
            onDeleteConfirmed = onDeleteConfirmed,
            selectedDiary = uiState.selectedDiary,
            moodName = moodName,
            onDateTimeUpdated = onDateTimeUpdated
        )
    }, content = {
        WriteContent(
            paddingValues = it,
            pagerState = pagerState,
            title = uiState.title,
            description = uiState.description,
            onDescriptionChanged = onDescriptionChanged,
            onTitleChanged = onTitleChanged,
            uiState = uiState,
            onSavedClick = onSavedClick
        )
    })
}