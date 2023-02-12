package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.ImageToUploadDao
import com.example.diaryapp.data.database.entity.ImageToUpload
import com.example.diaryapp.data.repository.MongoDb
import com.example.diaryapp.navigation.Screen
import com.example.diaryapp.navigation.SetupNavGraph
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.retryToDeletingImageFromFirebase
import com.example.diaryapp.util.retryUploadingImageToFirebase
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var keepSplashOpened = true
    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        installSplashScreen().setKeepOnScreenCondition{
            keepSplashOpened
        }
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    //startDestination = Screen.Authentication.route,
                    startDestination = getStartDestination(),
                    navController = navController
                ){
                    keepSplashOpened = false
                }
            }
        }
        cleanUpCheck(scope = lifecycleScope,imageToUploadDao,imageToDeleteDao)
    }

}

private fun cleanUpCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
){
    scope.launch (Dispatchers.IO){
        val result = imageToUploadDao.getAllImages()
        result.forEach { imageToUpload ->
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch (Dispatchers.IO) {
                        imageToUploadDao.cleanupImage(imageId = imageToUpload.id)
                    }
                }
            )
        }

        val result2 = imageToDeleteDao.getAllImages()
        result2.forEach {imageToDelete->
            retryToDeletingImageFromFirebase(
                imageToDelete = imageToDelete,
                onSuccess = {
                    scope.launch (Dispatchers.IO) {
                        imageToUploadDao.cleanupImage(imageId = imageToDelete.id)
                    }
                }
            )
        }
    }
}

private fun getStartDestination(): String {
    //checking user is already logged in or not
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}

