package com.example.audioroomdemozego

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.entity.ZegoEngineProfile

class MyApplication:Application() {
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var firebaseDataBase: FirebaseDatabase
   lateinit var firebaseAuth: FirebaseAuth
   lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var firebaseStorageReference: StorageReference
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDataBase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseAnalytics = Firebase.analytics
        firebaseStorageReference = firebaseStorage.reference



    }
}