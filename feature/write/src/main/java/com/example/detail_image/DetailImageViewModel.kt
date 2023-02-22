package com.example.detail_image

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.mongo.database.ImageToDeleteDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.mongo.repository.MongoDb
import com.example.ui.GalleryImage
import com.example.ui.GalleryState

import com.example.util.Constants
import com.example.util.model.RequestState
import com.example.write.UiState
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "DetailImageViewModel"
@HiltViewModel
class DetailImageViewModel @Inject constructor  (
 private val savedStateHandle: SavedStateHandle,
 private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    private val _state = mutableStateOf(Detail_ImgState())
    val state: State<Detail_ImgState> = _state

    val galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set

    init {
        getSelectedImage()
    }

    fun getSelectedImage() {

        val galleryImage = savedStateHandle.get<GalleryImage>(
            key = Constants.DETAIL_IMAGE_SCREEN_ARGUMENT_KEY
        )
        if (galleryImage != null) {
            _state.value.copy(
                selctedImage = galleryImage.image
            )
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null) {
                val result = MongoDb.deleteDiary(id = ObjectId.from(uiState.selectedDiaryId!!))

                if (result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        uiState.selectedDiary?.let { deleteImagesFromFirebase(images = it.images) }
                        onSuccess()
                    }
                } else if (result is RequestState.Error) {
                    withContext(Dispatchers.Main) {
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }
    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete().addOnFailureListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToDeleteDao.addImageToDelete(
                            ImageToDelete(remoteImagePath = remotePath)
                        )
                    }
                }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete().addOnFailureListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        imageToDeleteDao.addImageToDelete(
                            ImageToDelete(remoteImagePath = remotePath)
                        )
                    }
                }
            }
        }
    }
}