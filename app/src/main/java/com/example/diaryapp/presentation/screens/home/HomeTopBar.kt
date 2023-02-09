@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.diaryapp.presentation.screens.home


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeTopBar(
    onMenuClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = {
                onMenuClicked()
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Hamburger Menu Icons",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Text(text = "Diary")
        },
        actions = {
            IconButton(onClick = {
                onMenuClicked
            }) {
                Icon(//filter our diary by date
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Menu Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(
        onMenuClicked = {},
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}