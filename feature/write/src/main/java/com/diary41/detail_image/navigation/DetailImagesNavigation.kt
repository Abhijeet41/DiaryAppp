package com.diary41.detail_image.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.diary41.SharedViewModel
import com.diary41.detail_image.DetailImageScreen
import com.diary41.ui.GalleryImage
import com.diary41.util.Constants
import com.diary41.util.Screen

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