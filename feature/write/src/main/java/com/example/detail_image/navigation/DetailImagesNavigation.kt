package com.example.detail_image.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.SharedViewModel
import com.example.detail_image.DetailImageScreen
import com.example.ui.GalleryImage
import com.example.util.Constants
import com.example.util.Screen
import com.example.write.WriteViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun NavGraphBuilder.detail_imageRoute(
    onBackPressed: () -> Unit,
    navController: NavHostController,
    sharedViewmodel: SharedViewModel,

    ) {

    composable(
        route = Screen.DetailImage.route,
        arguments = listOf(navArgument(name = Constants.DETAIL_IMAGE_SCREEN_ARGUMENT_KEY) {
            type = NavType.ParcelableType(GalleryImage::class.java)
            nullable = true
            defaultValue = null
        })
    ) {


        DetailImageScreen(navController, sharedViewmodel)
    }
}