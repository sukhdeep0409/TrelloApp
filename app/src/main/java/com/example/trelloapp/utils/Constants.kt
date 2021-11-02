package com.example.trelloapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.trelloapp.activities.MyProfileActivity

object Constants {

    const val ASSIGNED_TO: String = "assignedTo"
    const val USERS: String = "Users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val DOCUMENT_ID: String = "documentID"
    const val ID: String = "id"
    const val EMAIL: String = "email"

    const val BOARDS: String = "boards"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "board_detail"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "unSelect"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val SHARED_PREFERENCES = "Shared_Prefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAFao8_pE:APA91bHdpZfOM-RfAjbGy5Jval6o0ALEFTB6cGSlctCJB3a16Xb8t9B55-NCbfTqgo_7qs44VC0uFudSw1soLPsYe2M_N2VtOte9uw05bDl1SpEZow7-kuzpUgf7B8OTq-garTNTsJys"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri): String? {
        return MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }
}