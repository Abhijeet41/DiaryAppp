package com.example.diaryapp.navigation

import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.SharedViewModel

import com.example.auth.navigation.authenticationRoute
import com.example.detail_image.navigation.detail_imageRoute
import com.example.home.navigation.homeRoute
import com.example.util.Constants.DETAIL_IMAGE_SCREEN_ARGUMENT_KEY
import com.example.util.Screen
import com.example.write.navigation.writeRoute


private const val TAG = "NavGraph"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController, onDataLoaded: () -> Unit
) {
    val sharedViewmodel: SharedViewModel = viewModel()

    NavHost(
        startDestination = startDestination, navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }, onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            }, navigateToWriteWithArgs = { id ->
                navController.navigate(Screen.Write.passDiaryId(id))
            }, navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            }, onDataLoaded = onDataLoaded
        )
        writeRoute(onBackPressed = {
            navController.popBackStack()
        }, onNavigationDetailImages = { selectedImage ->
            navController.currentBackStackEntry?.arguments
                ?.putParcelable(DETAIL_IMAGE_SCREEN_ARGUMENT_KEY, selectedImage)

            navController.navigate(Screen.DetailImage.route)
            Log.d(TAG, "selectedImage:${selectedImage.image.toString()} ")
        }, sharedViewmodel = sharedViewmodel)

        detail_imageRoute(onBackPressed = {
            navController.popBackStack()
        }, navController = navController,
            sharedViewmodel = sharedViewmodel
        )
    }
}




