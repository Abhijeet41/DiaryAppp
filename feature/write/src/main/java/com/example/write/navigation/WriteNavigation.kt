package com.example.write.navigation

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.util.Constants
import com.example.util.Screen
import com.example.util.model.Mood
import com.example.write.WriteScreen
import com.example.write.WriteViewModel

private const val TAG = "WriteNavigation"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {

    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = Constants.WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val pagerState = rememberPagerState()
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val pageNumber by remember {
            derivedStateOf { pagerState.currentPage }
        }
        val context = LocalContext.current
        val galleryState = viewModel.galleryState
        LaunchedEffect(key1 = uiState) {
            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }

        WriteScreen(onBackPressed = onBackPressed,
            moodName = {
                Mood.values()[pageNumber].name
            },
            onDeleteConfirmed = {
                viewModel.deleteDiary(onSuccess = {
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }, onError = { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                })
            },
            pagerState = pagerState,
            uiState = uiState,
            onTitleChanged = {
                viewModel.setTitle(title = it)
            },
            onDescriptionChanged = {
                viewModel.setDescription(description = it)
            },
            onSavedClick = { diary ->
                viewModel.upsertDiary(diary = diary.apply {
                    mood = Mood.values()[pageNumber].name
                }, onSuccess = {
                    onBackPressed()
                }, onError = { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "writeRoute: $errorMsg")
                })
            },
            onDateTimeUpdated = { viewModel.updateDateTime(it) },
            galleryState = galleryState,
            onImageSelect = { uri: Uri ->
                val type = context.contentResolver.getType(uri)?.split("/")?.last() ?: ".jpg"
                Log.d(TAG, "uri: $uri")
                viewModel.addImage(image = uri, imageType = type)
            },
            onImageDeleteClicked = { image ->
                galleryState.removeImage(galleryImage = image)
            }
        )
    }


}