package com.fburaky.teinapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception
import java.sql.Timestamp
import java.util.*


class ProfileFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    var selectedPicture : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getDataFromFirestore()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            selectedPicture = data.data

            try {

                if (selectedPicture != null){

                    if(Build.VERSION.SDK_INT >= 28){

                        val source = ImageDecoder.createSource(requireActivity().contentResolver , selectedPicture!!)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(bitmap)

                    }
                    else{
                        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver , selectedPicture)
                        imageView.setImageBitmap(bitmap)
                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sign_out_fragment_button.setOnClickListener {
            auth.signOut()
            val action = ProfileFragmentDirections.actionProfileFragmentToİnFragment()
            Navigation.findNavController(it).navigate(action)
        }

        imageView.setOnClickListener {

            if (ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1)
            }
            else{
                val intent = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }

        save_button.setOnClickListener {

            val storage = FirebaseStorage.getInstance()
            val reference = storage.reference

            //UUID - image name
            val uuid = UUID.randomUUID()
            val imageName = "$uuid.jpg"
            //val imagesReference = reference.child("images").child("image.jpg")
            val imagesReference = reference.child("images").child(imageName)


            if (selectedPicture != null){
                imagesReference.putFile(selectedPicture!!).addOnSuccessListener { taskSnapshot ->

                    //Database- Firestore
                    val uploadedPictureReference = FirebaseStorage.getInstance().reference.child("images").child(imageName)
                    uploadedPictureReference.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        val postMap = hashMapOf<String , Any>()

                        postMap.put("downloadUrl" , downloadUrl)
                        postMap.put("userEmail", auth.currentUser!!.email.toString())
                        postMap.put("name",name_text_profile_fragment.text.toString())
                        postMap.put("surname",surname_text_profile_fragment.text.toString())
                        postMap.put("phone",phone_text_profile_fragment.text.toString())
                        postMap.put("address",addres_text_profile_fragment.text.toString())
                        postMap.put("date", com.google.firebase.Timestamp.now())
                        // Name - Surname - Phone - Address

                        db.collection("Posts" + auth!!.currentUser!!.email.toString()).add(postMap).addOnCompleteListener { task ->

                            if (task.isComplete && task.isSuccessful){
                                Toast.makeText(activity,"Registration Successful" , Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener {    exception ->
                            Toast.makeText(activity,exception.localizedMessage.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(activity,"Selected Image Null" , Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getDataFromFirestore(){



        db.collection("Posts"+auth.currentUser!!.email.toString()).orderBy("date").addSnapshotListener { snapshot, exception ->

            if (exception != null){
                Toast.makeText( activity , exception.localizedMessage.toString() , Toast.LENGTH_LONG).show()
            }
            else{

                if (snapshot != null){
                    if (!snapshot.isEmpty){



                        val documents = snapshot.documents
                        for (document in documents){
                            val nameDb = document.get("name") as String
                            val surnameDb = document.get("surname") as String
                            val phoneDb = document.get("phone") as String
                            val addressDb = document.get("address") as String
                            val downloadUrlDb = document.get("downloadUrl") as String

                            name_text_profile_fragment.setText(nameDb)
                            surname_text_profile_fragment.setText(surnameDb)
                            phone_text_profile_fragment.setText(phoneDb)
                            addres_text_profile_fragment.setText(addressDb)
                            Picasso.get().load(downloadUrlDb).into(this.imageView)
                        }
                    }
                }

            }
        }
    }
}