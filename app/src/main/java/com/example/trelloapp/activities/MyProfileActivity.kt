package com.example.trelloapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloapp.R
import com.example.trelloapp.Database.FireStoreClass
import com.example.trelloapp.models.User
import com.example.trelloapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.et_email
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var mUserDetails: User

    private var mSelectedImageFileURI: Uri? = null
    private var mProfileImageURI: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()
        FireStoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            }
            else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update.setOnClickListener {
            if (mSelectedImageFileURI != null) {
                uploadUserImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.my_profile)

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user

        Glide.with(this@MyProfileActivity)
                .load(user.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)

        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileURI != null) {
            val sRef: StorageReference =
                    FirebaseStorage
                            .getInstance().reference.child(
                                    "USER_IMAGE" +
                                            System.currentTimeMillis() +
                                            "." +
                                            Constants.getFileExtension(this, mSelectedImageFileURI!!))

            sRef.putFile(mSelectedImageFileURI!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                        "FIREBASE_IMAGE_URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("DOWNLOADABLE_IMAGE_URI", uri.toString())
                    mProfileImageURI = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        
        if (mProfileImageURI.isNotEmpty()
                and (mProfileImageURI != mUserDetails.image)) {
            userHashMap[Constants.IMAGE] = mProfileImageURI
        }

        if (et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_mobile.text.toString().toLong() != mUserDetails.mobile) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        FireStoreClass().updateUserProfileData(this, userHashMap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((resultCode == Activity.RESULT_OK)
                and (requestCode == Constants.PICK_IMAGE_REQUEST_CODE)
                and (data!!.data != null)
        ){
            mSelectedImageFileURI = data.data

            try {
                Glide.with(this@MyProfileActivity)
                        .load(mSelectedImageFileURI)
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(iv_profile_user_image)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty())
                    and (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ){
                Constants.showImageChooser(this)
            }
        }
        else {
            //TODO Toast
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}