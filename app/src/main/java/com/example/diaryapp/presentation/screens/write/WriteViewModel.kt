package com.example.diaryapp.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.ImageToUploadDao
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.database.entity.ImageToUpload
import com.example.diaryapp.data.repository.MongoDb
import com.example.diaryapp.model.*
import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diaryapp.util.RequestState
import com.example.diaryapp.util.fetchImagesFromFirebase
import com.example.diaryapp.util.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

private const val TAG = "WriteViewModel"

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    val galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {   //This means Insert new diary or update existing one
        viewModelScope.launch {
            if (uiState.selectedDiaryId != null) {
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            } else {
                insertDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }

            //galleryState.clearImagesToBeDeleted()
        }
    }

    fun fetchSelectedDiary() {
        if (uiState.selectedDiaryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDb.getSelectedId(
                    diaryId = ObjectId.from(uiState.selectedDiaryId!!)
                ).catch {
                    emit(RequestState.Error(Exception("Diary is already deleted")))
                }.collect { diary ->
                    if (diary is RequestState.Success) {
                        setSelectedDiary(diary = diary.data)
                        setTitle(diary.data.title)
                        setDescription(diary.data.description)
                        setMood(Mood.valueOf(diary.data.mood))
                        //fetch images from firebase storage
                        fetchImagesFromFirebase(
                            remoteImagePaths = diary.data.images,
                            onImageDownload = { downloadImages ->
                                galleryState.addImage(
                                    GalleryImage(
                                        image = downloadImages,
                                        remoteImagePath = extractImagePath(
                                            fullImage = downloadImages.toString()
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDb.insertDiary(diary = diary.apply {
            if (uiState.updatedDateTime != null) {
                withContext(Dispatchers.Main) {
                    date = uiState.updatedDateTime!!
                }
            }
        })

        if (result is RequestState.Success) {
            uploadImageToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDb.updateDiary(diary = diary.apply {
            _id = ObjectId.Companion.from(uiState.selectedDiaryId!!)
            date = if (uiState.updatedDateTime != null) { //if user select date from date picker
                withContext(Dispatchers.Main) {
                    uiState.updatedDateTime!!
                }
            } else {//this is old already selected date while update diary
                withContext(Dispatchers.Main) {
                    uiState.selectedDiary!!.date
                }
            }
        })
        if (result is RequestState.Success) {
            uploadImageToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
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

    fun addImage(image: Uri, imageType: String) {
        //basically we are fetching images from firebase storage.
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.${imageType}"
        Log.d(TAG, remoteImagePath)

        galleryState.addImage(
            GalleryImage(image = image, remoteImagePath = remoteImagePath)
        )
    }

    private fun uploadImageToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
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

    private fun extractImagePath(fullImage: String): String {
        val chunks = fullImage.split("%2F")
        val imageName = chunks[2].split("?").first()
        Log.d(TAG, "images/${Firebase.auth.currentUser?.uid}/$imageName")
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

}

data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null
)