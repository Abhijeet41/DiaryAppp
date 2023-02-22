@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.write

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.SharedViewModel
import com.example.util.model.Diary
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.util.model.Mood
import com.google.firebase.auth.FirebaseAuth
import java.time.ZonedDateTime

private const val TAG = "WriteScreen"

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun WriteScreen(
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
    onImageDeleteClicked: (GalleryImage) -> Unit,
    onNavigationDetailImages: (GalleryImage) -> Unit,
    sharedViewmodel: SharedViewModel
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
        Log.d(TAG, "Size: ${galleryState.images.size.toString()}")
        sharedViewmodel.addGalleryImages(galleryState.images)

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
            onImageClicked = {
                selectedGalleryImage = it
                //  onNavigationDetailImages(it)
            }
        )
        AnimatedVisibility(visible = selectedGalleryImage != null) {
            Dialog(
                onDismissRequest = { selectedGalleryImage = null },
                properties = DialogProperties()
            ) {
                if (selectedGalleryImage != null) {
                    ZoomableImage(
                        selectedGalleryImage = selectedGalleryImage!!,
                        onCloseClicked = { selectedGalleryImage = null },
                        onDeleteClicked = {
                            if (selectedGalleryImage != null) {
                                onImageDeleteClicked(selectedGalleryImage!!)
                                selectedGalleryImage = null
                            }
                        },
                        galleryState = galleryState
                    )
                }
            }

        }
    })
}


@Composable
internal fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    galleryState: GalleryState
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
        val pagerState = rememberPagerState()
        val context = LocalContext.current
        HorizontalPager(
            pageCount = galleryState.images.size,
            state = pagerState,
            //   modifier = Modifier.weight(1f)
        ) { currentPage ->
            val currentImageUri = galleryState.images[currentPage].image
            val type = context.contentResolver.getType(currentImageUri)?.split("/")?.last() ?: ".jpg"

            val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                    "${currentImageUri.lastPathSegment}-${System.currentTimeMillis()}.${type}"
            Log.d(TAG, remoteImagePath)

            selectedGalleryImage.copy(
                image = currentImageUri,
                remoteImagePath = remoteImagePath
            )

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
                    .data(currentImageUri)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Fit,
                contentDescription = "Gallery Image"
            )

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon"
                )
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon"
                )
                Text(text = "Delete")
            }
        }
    }

}