package com.example.diaryapp.data.repository

import android.security.keystore.UserNotAuthenticatedException
import com.example.diaryapp.model.Diary
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.Diaries
import com.example.diaryapp.util.RequestState
import com.example.diaryapp.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

object MongoDb : MongoRepository {

    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>("ownerId == $0", user.identity),
                        name = "List of user's Diaries" //Its an optional but it define name of our subscription
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        if (user != null) {
            try {
                return realm.query<Diary>(query = "ownerId == $0", user.identity)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow().map {result->
                        RequestState.Success(data = result.list.groupBy {
                            //we need to do this because we don't have local date
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        })
                    }
            }catch (e: Exception){
                return flow { emit(RequestState.Error(e)) }
            }
        } else {
            return flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }
}