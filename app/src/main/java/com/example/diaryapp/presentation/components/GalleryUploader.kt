@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.diaryapp.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diaryapp.model.GalleryImage
import com.example.diaryapp.model.GalleryState
import com.example.diaryapp.ui.theme.Elevation
import kotlin.math.max

@Composable
fun GalleryUploader(
    modifier: Modifier = Modifier,
    galleryState: GalleryState,
    imageSize: Dp = 60.dp,
    imageShape: CornerBasedShape = Shapes().medium,
    spaceBetween: Dp = 12.dp,
    onAddClicked: () -> Unit,
    onImageSelect: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit
) {
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { images ->
        images.forEach {
            onImageSelect(it)
        }
    }


    BoxWithConstraints(modifier = modifier) {
        /* Calculation for b in such a way that
           maxWidth = 350,spaceBetween = 10,imagesSize = 40
           b = maxwidth / 10 + 40 - 1    we did minus 1 because last box should be number line +2 or +5
           b = 6
        */
        val numberOfVisibleImages = remember {
            derivedStateOf {
                max(
                    a = 0,
                    b = this.maxWidth.div(spaceBetween + imageSize).toInt().minus(1)
                )
            }
        }

        val remainingImages = remember {
            derivedStateOf {
                galleryState.images.size - numberOfVisibleImages.value
            }
        }

        Row {

            AddImageButton(
                imageSize = imageSize,
                imagesShape = imageShape,
                onClick = {
                    onAddClicked()
                    multiplePhotoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.width(spaceBetween))

            galleryState.images.take(numberOfVisibleImages.value).forEach { galleryImage ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize)
                        .clickable {
                           onImageClicked(galleryImage)
                        },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(galleryImage.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Gallery Image",
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if (remainingImages.value > 0) {
                LastImageOverlay(
                    imageSize = imageSize,
                    remainingImages = remainingImages.value,
                    imageShap = imageShape
                )
            }

        }
    }
}

@Composable
fun AddImageButton(
    imageSize: Dp,
    imagesShape: CornerBasedShape,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(imageSize)
            .clip(shape = imagesShape),
        onClick = onClick,
        tonalElevation = Elevation.Level1
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Icon"
            )
        }
    }
}