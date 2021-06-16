package com.fburaky.teinapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.fragment_in.*


class InFragment : Fragment() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val config = TwitterConfig.Builder(requireContext())
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(getString(R.string.Api_key),getString(R.string.Api_Secreat)))
            .debug(true)
            .build()

        Twitter.initialize(config)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Tanımlamar gerçekleştiğinde yapılmasını istediğimiz işlemleri buraya yazıyoruz ...
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser

        if (currentUser != null){
            // Kullanıcı önceden giriş yaptıysa bilgileri alıp profil fragmanınıa giriş yapıyoruz
            val action = InFragmentDirections.actionİnFragmentToProfileFragment()
            Navigation.findNavController(view).navigate(action)
        }

        sign_up_fragment_button.setOnClickListener {

            val action = InFragmentDirections.actionİnFragmentToUpFragment()
            Navigation.findNavController(it).navigate(action)
        }

        sign_in_button.setOnClickListener {

            val emailInFragment = email_text_In_fragment.text.toString()
            val passwordInFragment = password_text_In_fragment.text.toString()

            if (emailInFragment.isNotEmpty() && passwordInFragment.isNotEmpty()){

                auth.signInWithEmailAndPassword(emailInFragment, passwordInFragment).addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        //Sign-In
                        Toast.makeText(
                            activity,
                            "Welcome : ${auth.currentUser?.email.toString()}",
                            Toast.LENGTH_LONG
                        ).show()
                        val action = InFragmentDirections.actionİnFragmentToProfileFragment()
                        Navigation.findNavController(it).navigate(action)
                    }
                }.addOnFailureListener { exception ->

                    Toast.makeText(
                        activity,
                        exception.localizedMessage.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                }

            }
            else{
                Toast.makeText(activity, "Please Enter Your Email and Password", Toast.LENGTH_LONG).show()
            }
        }

        buttonTwitter.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {

                /*
                // Do something with result, which provides a TwitterSession for making API calls
                val session = TwitterCore.getInstance().sessionManager.activeSession
                val credential = TwitterAuthProvider.getCredential(session.authToken.token,session.authToken.secret)
                auth.signInWithCredential(credential)
                 */

                val credential = TwitterAuthProvider.getCredential(result.data.authToken.token ,result.data.authToken.secret)
                auth!!.signInWithCredential(credential)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val action = InFragmentDirections.actionİnFragmentToProfileFragment()
                                Navigation.findNavController(view).navigate(action)

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(activity, "Authentication failed.",Toast.LENGTH_SHORT).show()
                            }
                        }
            }

            override fun failure(exception: TwitterException) {
                // Do something on failure
                Toast.makeText(activity, exception.localizedMessage.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        buttonTwitter.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }
}
