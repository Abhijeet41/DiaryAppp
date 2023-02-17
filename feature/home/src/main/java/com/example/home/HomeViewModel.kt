package com.example.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mongo.database.ImageToDeleteDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.util.connectivity.ConnectivityObserver
import com.example.util.connectivity.NetworkConnectivityObserver
import com.example.mongo.repository.MongoDb
import com.example.util.Diaries
import com.example.util.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.time.ZonedDateTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    var dateIsSelected by mutableStateOf(false)
        private set

    init {
        getDiaries()
        //observeAllDiaries() we remove from here later
        viewModelScope.launch {
            connectivity.observer().collect { network = it }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDiaries(zonedDateTime: ZonedDateTime? = null) {
        dateIsSelected = zonedDateTime != null
        diaries.value = RequestState.Loading
        if (dateIsSelected && zonedDateTime != null) {//this one is filtered diaries
            observeFilteredDiaries(zonedDateTime)
        } else { //this one is observe default diaries
            observeAllDiaries()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeAllDiaries() {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        //when ever we wants to observe allDiaries(Default) then we need to cancel other job(filteredDiaryJob)
        allDiariesJob = viewModelScope.launch {
            if (::filteredDiariesJob.isInitialized) {
                filteredDiariesJob.cancelAndJoin()
            }
            MongoDb.getAllDiaries().collect() { result ->
                diaries.value = result
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime) {

        filteredDiariesJob = viewModelScope.launch {
            //when ever we wants to observe filterDiaries then we need to cancel other job
            if (::allDiariesJob.isInitialized) {
                allDiariesJob.cancelAndJoin()
            }
            MongoDb.getFilteredDiaries(zonedDateTime = zonedDateTime).collect { result ->
                diaries.value = result
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.Available) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imageDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imageDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete().addOnFailureListener {
                            /*
                                In case of failed to delete images then add that failed images in to database
                                so we can later delete those images when internet connection is on
                             */
                            viewModelScope.launch(Dispatchers.IO) {
                                imageToDeleteDao.addImageToDelete(
                                    ImageToDelete(remoteImagePath = imagePath)
                                )
                            }
                        }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDb.deleteAllDiaries()
                        if (result is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else if (result is RequestState.Error) {
                            withContext(Dispatchers.Main) {
                                onError(result.error)
                            }
                        }
                    }
                }.addOnFailureListener {
                    onError(it)
                }
        } else {
            onError(Exception("No Internet Connection."))
        }
    }

}