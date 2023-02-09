@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.diaryapp.presentation.screens.home

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.diaryapp.R
import com.example.diaryapp.model.Diary
import com.example.diaryapp.util.Diaries
import com.example.diaryapp.util.RequestState
import java.time.LocalDate

private const val TAG = "HomeScreen"

@ExperimentalFoundationApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    onSignedOutClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    diaries: Diaries
) {
    var padding by remember { mutableStateOf(PaddingValues()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    NavigationDrawer(drawerState = drawerState, onSignedOutClicked = onSignedOutClicked) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(
                    onMenuClicked = onMenuClicked,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(
                        end = padding.calculateEndPadding(
                            LayoutDirection.Ltr
                        )
                    ), onClick = navigateToWrite
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Diary Icons"
                    )
                }
            },
            content = {
                padding = it
                when (diaries) {
                    is RequestState.Error -> {
                        EmptyPage(title = "Error", subtitle = "${diaries.error.message}")
                    }
                    is RequestState.Success -> {
                        HomeContent(
                            diaryNotes = diaries.data,
                            onClick =navigateToWriteWithArgs,
                            paddingValues = it
                        )
                    }
                    is RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is RequestState.Idle -> {
                        Log.d(TAG, "HomeScreen: ")
                    }
                }
            },
        )
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignedOutClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier.size(250.dp),
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "logo image"
                        )
                    }
                    NavigationDrawerItem(
                        label = {
                            Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google Logo"
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Sign Out", color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        selected = false,
                        onClick = onSignedOutClicked
                    )
                }
            )
        },
        content = content
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalFoundationApi::class)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        onMenuClicked = {},
        onSignedOutClicked = {},
        navigateToWrite = {},
        navigateToWriteWithArgs = {},
        diaries = RequestState.Idle
    )
}