package com.example.util

import com.example.util.model.Diary
import com.example.util.model.RequestState
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

object Constants {

    const val APP_ID = "mydiaryapp-lkohc"
    const val CLIENT_ID = "604141205154-hp9ri5ja2b584mcc7einosv1a9to7f74.apps.googleusercontent.com"

    const val WRITE_SCREEN_ARGUMENT_KEY = "diaryId"

    //db_name
    const val IMAGE_DATABASE = "image_db"
    //db_table_name
    const val IMAGE_TO_UPLOAD_TABLE = "images_to_upload_table"
    const val IMAGE_TO_DELETE_TABLE = "images_to_delete_table"


}