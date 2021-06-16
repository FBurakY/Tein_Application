package com.fburaky.teinapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_up.*


class UpFragment : Fragment() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Oturum işlemi için objemi oluşturuyorum .
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_up, container, false)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sign_up_continue_button.setOnClickListener {

            val email = email_text_view.text.toString()
            val password = password_text_view.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){

                auth.createUserWithEmailAndPassword(email , password).addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        // Oturum işlemleri başarılı olma durumu
                        Toast.makeText(activity ,"Login request successful",Toast.LENGTH_LONG).show()
                        val action = UpFragmentDirections.actionUpFragmentToİnFragment()
                        Navigation.findNavController(it).navigate(action)
                    }

                }.addOnFailureListener { exception ->

                    if (exception != null){
                        // FireBase'den gelen hata mesajı !
                        Toast.makeText(activity ,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                    }
                }
            }

            else{ // Email & Password boş ilse uyarı mesajı verecek !
                Toast.makeText(activity ,"Please Enter Your Email and Password !",Toast.LENGTH_LONG).show()
            }
        }
    }
}