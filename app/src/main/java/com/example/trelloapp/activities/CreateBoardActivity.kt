package com.example.trelloapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloapp.R
import com.example.trelloapp.Database.FireStoreClass
import com.example.trelloapp.models.Board
import com.example.trelloapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private var mSelectedImageFileURI: Uri? = null

    private lateinit var mUsername: String

    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setUpActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUsername = intent.getStringExtra(Constants.NAME).toString()
        }

        iv_board_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
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

        btn_create.setOnClickListener {
            if (mSelectedImageFileURI != null) {
                uploadBoardImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_create_board_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar?.title = resources.getString(R.string.create_board_title)

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun createBoard() {
        val assignedUsersList: ArrayList<String> = ArrayList()
        assignedUsersList.add(getCurrentUserID())

        val boardObject = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUsername,
            assignedUsersList
        )

        FireStoreClass().createBoard(this, boardObject)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference =
            FirebaseStorage.getInstance()
                .reference
                .child(
                    "BOARD_IMAGE"
                            + System.currentTimeMillis()
                            + "."
                            + Constants.getFileExtension(
                        this,
                        mSelectedImageFileURI!!
                    )
                )

        sRef.putFile(mSelectedImageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Board_Image_Url",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.i("Downloadable_Image_Url", uri.toString())
                        mBoardImageURL = uri.toString()

                        createBoard()
                }
            }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((resultCode == Activity.RESULT_OK)
            and (requestCode == Constants.PICK_IMAGE_REQUEST_CODE)
            and (data!!.data != null)
        ){
            mSelectedImageFileURI = data.data

            try {
                Glide.with(this)
                    .load(mSelectedImageFileURI)
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
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
}