@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)

package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.diaryapp.model.Diary
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    onBackPressed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    selectedDiary: Diary?,
    pagerState: PagerState,
) {
    Scaffold(topBar = {
        WriteTopBar(
            onBackPressed = onBackPressed,
            onDeleteConfirmed = onDeleteConfirmed,
            selectedDiary = selectedDiary
        )
    }, content = {
        WriteContent(
            paddingValues = it,
            pagerState = pagerState,
            title = "",
            description = "",
            onDescriptionChanged = {} ,
            onTitleChanged = {}
        )
    })
}