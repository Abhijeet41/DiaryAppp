@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalPagerApi::class
)

package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.GalleryImage
import com.example.diaryapp.model.GalleryState
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
    moodName: () -> String,
    onSavedClick: (Diary) -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
    galleryState: GalleryState,
    onImageSelect: (Uri) -> Unit,
    onImageDeleteClicked: (GalleryImage) -> Unit
) {
    var selectedGalleryImage by remember { mutableStateOf<GalleryImage?>(null) }
    //update the Mood when selecting an existing Diary
    LaunchedEffect(key1 = uiState.mood) {
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
    }, content = { paddingValues ->
        WriteContent(
            paddingValues = paddingValues,
            pagerState = pagerState,
            title = uiState.title,
            description = uiState.description,
            onDescriptionChanged = onDescriptionChanged,
            onTitleChanged = onTitleChanged,
            uiState = uiState,
            onSavedClick = onSavedClick,
            galleryState = galleryState,
            onImageSelect = onImageSelect,
            onImageClicked = { selectedGalleryImage = it }
        )
        AnimatedVisibility(visible = selectedGalleryImage != null) {
            Dialog(onDismissRequest = { selectedGalleryImage = null }) {
              if (selectedGalleryImage != null){
                  ZoomableImage(
                      selectedGalleryImage = selectedGalleryImage!!,
                      onCloseClicked = { selectedGalleryImage = null },
                      onDeleteClicked = {
                          if (selectedGalleryImage != null){
                              onImageDeleteClicked(selectedGalleryImage!!)
                              selectedGalleryImage = null
                          }
                      }
                  )
              }
            }
        }
    })
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Icon")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                Text(text = "Delete")
            }
        }
    }
}