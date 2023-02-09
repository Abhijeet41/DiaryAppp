package com.example.diaryapp.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Diary: RealmObject{
    //note we cannot use val inside this realm sdk
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    var ownerId: String = ""
    var mood: String = Mood.Neutral.name  //realm doesnot support enum directly so its type is string
    var title: String = ""
    var description: String = ""
    var images: RealmList<String> = realmListOf()
    var date: RealmInstant = RealmInstant.from(System.currentTimeMillis(),0)
}
