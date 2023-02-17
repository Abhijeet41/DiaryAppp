

package com.example.diaryapp.navigation

import android.os.Build

import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import com.example.auth.navigation.authenticationRoute
import com.example.home.navigation.homeRoute
import com.example.util.Screen
import com.example.write.navigation.writeRoute
import com.google.accompanist.pager.ExperimentalPagerApi


private const val TAG = "NavGraph"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(
    startDestination: String, navController: NavHostController, onDataLoaded: () -> Unit
) {
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
        })
    }
}




