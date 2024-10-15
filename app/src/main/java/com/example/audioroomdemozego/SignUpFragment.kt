package com.example.audioroomdemozego

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audioroomdemozego.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage


class SignUpFragment : Fragment() {
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseStorageReference:StorageReference
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var imageUri: Uri
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = Firebase.auth
        firebaseAnalytics = Firebase.analytics
        firebaseStorage = Firebase.storage
        firebaseStorageReference = Firebase.storage.reference

        binding.signUpTOlogin.setOnClickListener{
            val loginFragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container,loginFragment).addToBackStack(null).commit()
        }

        binding.signUpProfilePic.setOnClickListener{
            openImagePicker()
        }


        binding.signUpButtonCV.setOnClickListener{
            val email = binding.emailEditSignUp.text.toString()
            val name = binding.nameEditSignup.text.toString()
            val password = binding.passwordEditSignUp.text.toString()
            signUpUser(name,email,password)

        }
    }



    private fun signUpUser(name:String,email:String,password:String){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = firebaseAuth.currentUser?.uid
                val user = firebaseAuth.currentUser

                if (userId != null && user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { profileUpdateTask ->
                        if (profileUpdateTask.isSuccessful) {
                           uploadProfilePicture(userId)

                        }
                    }
                }
            }
        }
    }

    private fun uploadProfilePicture(userId:String){
        if(::imageUri.isInitialized){
            val ref = firebaseStorageReference.child("profile_pics").child(userId)
            ref.putFile(imageUri).addOnSuccessListener {taskSnapshot->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri->
                    val imageUrl = uri.toString()
                    saveProfileImageUri(userId,imageUrl)
                }
            }

        }
    }

    private fun saveProfileImageUri(userId: String,imageUrl:String){
        val user = firebaseAuth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(imageUrl)).build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener{task->
            if(task.isSuccessful){
//               resultDialog("Sign Up Successful", true)
                saveUserToDatabase(userId, user.displayName ?: "", user.email ?: "", imageUrl)
            }
            
        }
    }

    private fun saveUserToDatabase(userId: String, name: String, email: String, imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users").child(userId)

        val user = Users(userId, name, email, imageUrl) // Create your Users data class
        usersRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                resultDialog("Sign Up Successful", true)
            } else {
                Toast.makeText(requireContext(), "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openImagePicker(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select an image"),PICK_IMAGE_REQUEST)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openImagePicker()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!=null){

            imageUri = data.data!!
           binding.signUpProfilePic.setImageURI(imageUri)

        }
    }

    private fun resultDialog(message:String,isSuccessful: Boolean){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("$message")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id->
            dialog.dismiss()
            if(isSuccessful){
                val intent = Intent(requireContext(),HomeActivity::class.java)
                startActivity(intent)
            }
        })

        val dialog = builder.create()
        dialog.show()

    }

}