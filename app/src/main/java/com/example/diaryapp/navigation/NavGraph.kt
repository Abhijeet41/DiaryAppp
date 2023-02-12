@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalPagerApi::class
)

package com.example.diaryapp.navigation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diaryapp.model.GalleryImage
import com.example.diaryapp.model.Mood
import com.example.diaryapp.model.rememberGalleryState
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewmodel
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.home.HomeViewModel
import com.example.diaryapp.presentation.screens.write.WriteScreen
import com.example.diaryapp.presentation.screens.write.WriteViewModel
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diaryapp.util.RequestState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "NavGraph"

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
        homeRoute(navigateToWrite = {
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

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit, onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewmodel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onTokenIdReceived = { tokenId ->
                Log.d("authenticationRoute", tokenId)
                viewModel.signInWithMongoAtlas(tokenId = tokenId, onSuccess = {
                    messageBarState.addSuccess("Successfully Authenticated!")
                    viewModel.setLoading(false)
                }, onError = { errorMessage ->
                    messageBarState.addError(Exception(errorMessage))
                    viewModel.setLoading(false)
                    Log.d("errorMessage", errorMessage.message.toString())
                })
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(tokenId = tokenId, onSuccess = {
                    messageBarState.addSuccess("Successfully Authenticated!")
                    viewModel.setLoading(false)
                }, onError = { errorMessage ->
                    messageBarState.addError(Exception(errorMessage))
                    viewModel.setLoading(false)
                })
            },
            onFailedFirebaseSignIn = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

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
        val viewmodel: HomeViewModel = viewModel()
        val diaries by viewmodel.diaries

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(drawerState = drawerState,
            onMenuClicked = {
                scope.launch { drawerState.open() }
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onSignedOutClicked = {
                signOutDialogOpened = true
            },
            diaries = diaries
        )
        DisplayAlertDialog(title = "SignOut",
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
            })
    }
}

fun NavGraphBuilder.writeRoute(onBackPressed: () -> Unit) {

    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
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
            onImageDeleteClicked = {image->
                galleryState.removeImage(galleryImage = image)
            }
        )
    }


}