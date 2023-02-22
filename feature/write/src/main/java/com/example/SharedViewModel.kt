package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.ui.GalleryImage
import com.example.ui.GalleryState

class SharedViewModel(

) : ViewModel(){

    var galleryState by mutableStateOf<SnapshotStateList<GalleryImage>?>(null)
        private set

    fun addGalleryImages(newGalleryState: SnapshotStateList<GalleryImage>){
        galleryState = newGalleryState
    }
}