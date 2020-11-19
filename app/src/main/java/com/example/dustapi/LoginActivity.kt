package com.example.dustapi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //로그인 화면
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener{
            if(editTextEmail.text.trim().isNotEmpty() || editTextPassword.text.trim().toString().isNotEmpty()){
                signInUser(editTextEmail.text.trim().toString(), editTextPassword.text.trim().toString())
            }else{
                Toast.makeText(this, "Input Required", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun signInUser(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task->

                if(task.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Error!!"+task.exception, Toast.LENGTH_LONG).show()
                }
            }
    }
}