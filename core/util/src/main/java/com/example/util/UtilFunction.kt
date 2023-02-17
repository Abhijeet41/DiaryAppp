package com.example.util

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

private const val TAG = "UtilFunction"
@RequiresApi(Build.VERSION_CODES.O)
fun RealmInstant.toInstant(): Instant {
    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond

    return if (sec >= 0) {
        Instant.ofEpochSecond(sec,nano.toLong())
    }else{
        Instant.ofEpochSecond(sec-1,1_000_000 + nano.toLong())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSecond
    val nano: Int = this.nano
    return if (sec >= 0) {
        RealmInstant.from(sec, nano)
    } else {
        RealmInstant.from(sec + 1, -1_000_000 + nano)
    }
}


fun fetchImagesFromFirebase(
    remoteImagePaths: List<String>,
    onImageDownload: (Uri) -> Unit,
    onImageDownloadFailed: (Exception) -> Unit = {},
    onReadyToDisplay:() -> Unit = {}
){
    if (remoteImagePaths.isNotEmpty()){
        remoteImagePaths.forEachIndexed { index, remoteImagePath ->
            if (remoteImagePath.trim().isNotEmpty()){
                FirebaseStorage.getInstance().reference.child(remoteImagePath.trim()).downloadUrl
                    .addOnSuccessListener { uri->
                        Log.d("DownloadUrl","$uri")
                        onImageDownload(uri)
                        if (remoteImagePaths.lastIndexOf(remoteImagePaths.last()) == index){
                            onReadyToDisplay()
                        }
                    }.addOnFailureListener {
                        onImageDownloadFailed(it)
                    }
            }
        }
    }
}