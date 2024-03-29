package com.howl.movingrestaurant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_grid.view.*
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.fragment_grid.*

class SignUpActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_grid)
        //auth = FirebaseAuth.getInstance()
        email_signin_button.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))

        }
    }
    override fun onStart() {
        super.onStart()
        //moveMainPage(auth?.currentUser) //자동 로그인 기능
    }
}      // signinAndSignup()//이메일 클릭 리스너 + 회원가입 리스너, 회원 정보 입력화면 추가
/*auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
?.addOnCompleteListener {
task ->
   if(task.isSuccessful){
       //Creating a user account
       moveMainPage(task.result?.user)
   }else if(task.exception?.message.isNullOrEmpty()){
       //Show the error message
       Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
   }else{
       //Login if you have account
       //signinEmail()
       Toast.makeText(getApplicationContext(),"이미 가입된 이메일 입니다.",Toast.LENGTH_LONG).show()
   }
}*/