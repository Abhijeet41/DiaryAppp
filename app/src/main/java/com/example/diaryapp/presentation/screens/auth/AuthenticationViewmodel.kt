package com.example.diaryapp.presentation.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.util.Constants.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewmodel @Inject constructor(

) : ViewModel(){

    var loadingState = mutableStateOf(false)
        private set

    fun setLoading(loading: Boolean){
        loadingState.value = loading
    }

    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: (Boolean) -> Unit,
        onError:(Exception) -> Unit
    ){
        viewModelScope.launch {
            try {
               val result = withContext(Dispatchers.IO){
                   App.create(APP_ID).login(
                       //Credentials.google(tokenId, GoogleAuthType.ID_TOKEN)
                       Credentials.jwt(tokenId)
                   ).loggedIn
               }
                withContext(Dispatchers.Main){
                    onSuccess(result)
                }
            }catch (e:Exception){
                withContext(Dispatchers.Main){
                    onError(e)
                }
            }
        }
    }

}