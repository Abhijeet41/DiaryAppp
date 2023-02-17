@file:OptIn(ExperimentalFoundationApi::class)

package com.example.home.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.HomeScreen
import com.example.home.HomeViewModel
import com.example.ui.components.DisplayAlertDialog
import com.example.util.Constants.APP_ID
import com.example.util.Screen
import com.example.util.model.RequestState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val drawerState = rememberDrawerState(
            initialValue = DrawerValue.Closed
        )
        val scope = rememberCoroutineScope()
        var signOutDialogOpened by remember { mutableStateOf(false) }
        var deleteDialogOpened by remember { mutableStateOf(false) }
        val viewmodel: HomeViewModel = hiltViewModel()
        val diaries by viewmodel.diaries
        val context = LocalContext.current

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch { drawerState.open() }
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onSignedOutClicked = {
                signOutDialogOpened = true
            },
            onDeleteAllClicked = {
                deleteDialogOpened = true
            },
            diaries = diaries,
            dateIsSelected = viewmodel.dateIsSelected,
            onDateReset = { viewmodel.getDiaries() },
            onDateSelected = {
                viewmodel.getDiaries(zonedDateTime = it)
            }
        )
        DisplayAlertDialog(
            title = "SignOut",
            message = "Are you sure you want to sign out ?",
            dialogOpened = signOutDialogOpened,
            onCloseDialog = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }
                }
            }
        )
        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all your diaries?",
            dialogOpened = deleteDialogOpened,
            onCloseDialog = { deleteDialogOpened = false },
            onYesClicked = {
                viewmodel.deleteAllDiaries(onSuccess = {
                    android.widget.Toast.makeText(context, "All diaries deleted", android.widget.Toast.LENGTH_SHORT).show()
                    scope.launch { drawerState.close() }//close NavigationDrawer after deleted diaries
                }, onError = {
                    android.widget.Toast.makeText(
                        context,
                        if (it.message.equals("No Internet Connection."))
                            "We need an Internet Connection for this operation "
                        else "",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    scope.launch { drawerState.close() }//close NavigationDrawer after deleted diaries
                })
            }
        )
    }
}