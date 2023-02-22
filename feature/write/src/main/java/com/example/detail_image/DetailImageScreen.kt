@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class
)

package com.example.detail_image

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.SharedViewModel
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.util.Constants.DETAIL_IMAGE_SCREEN_ARGUMENT_KEY
import com.example.write.UiState
import com.google.accompanist.pager.ExperimentalPagerApi


private const val TAG = "DetailImageScreen"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailImageScreen(
    navController: NavHostController,
    sharedViewmodel: SharedViewModel,
    ) {
    val galleryImage = navController.previousBackStackEntry?.arguments?.getParcelable(
        DETAIL_IMAGE_SCREEN_ARGUMENT_KEY,
        GalleryImage::class.java
    )

    val viewModel: DetailImageViewModel = hiltViewModel()
    val context = LocalContext.current
    val images = sharedViewmodel.galleryState
    Log.d(TAG, "galleryState : ${images?.size.toString()}")

    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            ZoomableImage(
                onDeleteClicked = {
                    viewModel.deleteDiary(onSuccess = {
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }, onError = { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    })
                },
                images = images
            )


        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
internal fun ZoomableImage(
    onDeleteClicked: () -> Unit,
    images: SnapshotStateList<GalleryImage>?,
) {
    val pagerState = rememberPagerState()

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

        images?.let {
            HorizontalPager(
                pageCount = it.size,
                state = pagerState,
                //   modifier = Modifier.weight(1f)
            ) { currentPage ->

                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(images[currentPage].image)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Gallery Image"
                )

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(onClick = {onDeleteClicked()}) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Icon"
                )
                Text(text = "Delete")
            }
        }
    }

}
