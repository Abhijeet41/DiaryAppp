@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.home


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun HomeTopBar(
    onMenuClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit
) {
    val dateDialog = rememberSheetState()
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }

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
            if (dateIsSelected) {
                IconButton(onClick = onDateReset) {
                    Icon(//filter our diary by date
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                IconButton(onClick = {
                    dateDialog.show()
                }) {
                    Icon(//filter our diary by date
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date Menu Icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )

    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate: LocalDate ->
            pickedDate = localDate
            onDateSelected(
                ZonedDateTime.of(
                    pickedDate,
                    LocalTime.now(),
                    ZoneId.systemDefault()
                )
            )
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(
        onMenuClicked = {},
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        onDateReset = {},
        onDateSelected = {},
        dateIsSelected = false
    )
}