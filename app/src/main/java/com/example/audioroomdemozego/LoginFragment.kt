package com.example.audioroomdemozego

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.audioroomdemozego.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage


class LoginFragment : Fragment() {
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseStorageReference: StorageReference
    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = (requireActivity().application as MyApplication).firebaseAuth
        firebaseStorage = (requireActivity().application as MyApplication).firebaseStorage
        firebaseStorageReference =  (requireActivity().application as MyApplication).firebaseStorageReference
        firebaseAnalytics =  (requireActivity().application as MyApplication).firebaseAnalytics

        binding.loginbuttoncv.setOnClickListener{
            val email = binding.emailEditLogin.text.toString()
            val password = binding.passwordEditLogin.text.toString()
            loginUser(email,password)
        }

        binding.loginTOSignUp.setOnClickListener{
            val signUpFragment = SignUpFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container,signUpFragment).addToBackStack(null).commit()
        }
    }


    private fun loginUser(email:String,password:String){
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful){
                resultDialog("Log in Successful",true)
            }else{
                resultDialog("Login Failed: ${task.exception?.message}", false)
            }

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