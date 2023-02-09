package com.example.diaryapp.util

import com.example.diaryapp.model.Diary
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate,List<Diary>>>

object Constants {

    const val APP_ID = "mydiaryapp-lkohc"
    const val CLIENT_ID = "604141205154-hp9ri5ja2b584mcc7einosv1a9to7f74.apps.googleusercontent.com"

    const val WRITE_SCREEN_ARGUMENT_KEY = "diaryId"
}