package com.example.dustapi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    //회원가입화면
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        btnRegister.setOnClickListener{
            if(editEmail.text.trim().toString().isNotEmpty() || editPassword.text.trim().toString().isNotEmpty()){
                createUser(editEmail.text.trim().toString(), editPassword.text.trim().toString())
            }else{
                Toast.makeText(this, "Input Required", Toast.LENGTH_LONG).show()
            }
        }
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    fun createUser(email:String, password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                    task->
                if(task.isSuccessful){
                    Log.e("Task Message", "Successful ..")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                }else{
                    Log.e("Task Message", "Failed.."+ task.exception)
                }
            }

    }

   /* override fun onStart() {
        super.onStart()
        val user = auth.currentUser

        if (user!=null){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }*/
}