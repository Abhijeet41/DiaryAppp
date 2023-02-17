package com.example.mongo.repository

import android.os.Build
import android.security.keystore.UserNotAuthenticatedException
import androidx.annotation.RequiresApi
import com.example.util.Constants
import com.example.util.model.Diary
import com.example.util.model.RequestState
import com.example.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MongoDb : MongoRepository {

    private val app = App.create(Constants.APP_ID)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllDiaries(): Flow<Diaries> {
        if (user != null) {
            try {
                return realm.query<Diary>(query = "ownerId == $0", user.identity)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow().map { result ->
                        RequestState.Success(data = result.list.groupBy {
                            //we need to do this because we don't have local date
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        })
                    }
            } catch (e: Exception) {
                return flow { emit(RequestState.Error(e)) }
            }
        } else {
            return flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override fun getSelectedId(diaryId: ObjectId): Flow<RequestState<Diary>> {
        if (user != null) {
            try {
                //find that single diary from list of diaries by using id
                return realm.query<Diary>(query = "_id == $0", diaryId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }

            } catch (e: Exception) {
                return flow { emit(RequestState.Error(e)) }
            }
        } else {
            return flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
        if (user != null) {
            return realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.identity })
                    RequestState.Success(data = addedDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            return RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        if (user != null) {
            return realm.write {
                val queriedDiary = query<Diary>(query = "_id == $0", diary._id).first().find()
                if (queriedDiary != null) {
                    queriedDiary.title = diary.title
                    queriedDiary.description = diary.description
                    queriedDiary.mood = diary.mood
                    queriedDiary.images = diary.images
                    queriedDiary.date = diary.date
                    RequestState.Success(data = queriedDiary)
                } else {
                    RequestState.Error(error = Exception("Queried diary does not exist."))
                }
            }
        } else {
            return RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(id: ObjectId): RequestState<Diary> {
        if (user != null) {
            return realm.write {
                val diary =
                    query<Diary>(query = "_id == $0 AND ownerId == $1", id, user.identity)
                        .first().find()
                if (diary != null) {
                    try {
                        delete(diary)
                        RequestState.Success(diary)
                    } catch (e: Exception) {
                        RequestState.Error(e)
                    }
                } else {
                    RequestState.Error(Exception("Diary does not exist"))
                }
            }
        } else {
            return RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteAllDiaries(): RequestState<Boolean> {
        if (user != null) {
            return realm.write {
                try {
                    val diaries = this.query<Diary>("ownerId == $0", user.identity).find()
                    delete(diaries)
                    RequestState.Success(data = true)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            return RequestState.Error(UserNotAuthenticatedException())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFilteredDiaries(zonedDateTime: ZonedDateTime): Flow<Diaries> {
        if (user != null) {
            try {
                //find that single diary from list of diaries by using id
                return realm.query<Diary>(
                    "ownerId == $0 AND date < $1 AND date > $2",
                    user.identity,
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate().plusDays(1),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset), 0
                    ),
                    RealmInstant.from(
                        LocalDateTime.of(
                            zonedDateTime.toLocalDate(),
                            LocalTime.MIDNIGHT
                        ).toEpochSecond(zonedDateTime.offset), 0
                    ),
                ).asFlow().map { result ->
                    RequestState.Success(
                        data = result.list.groupBy {
                            it.date.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                    )
                }
            } catch (e: Exception) {
                return flow { emit(RequestState.Error(e)) }
            }
        } else {
            return flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }
}