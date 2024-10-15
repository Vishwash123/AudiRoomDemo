package com.example.audioroomdemozego

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        firebaseAuth = (application as MyApplication).firebaseAuth
        val user = firebaseAuth.currentUser
        if(user!=null){
            startActivity(Intent(this,HomeActivity::class.java))
        }

        if(savedInstanceState == null){
            loadSignUpFragment()
        }

    }

    private fun loadSignUpFragment(){
        val signUpFragment = SignUpFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,signUpFragment).commit()
    }
}