package com.example.trelloapp.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.trelloapp.Database.FireStoreClass
import com.example.trelloapp.R
import com.example.trelloapp.activities.MainActivity
import com.example.trelloapp.activities.SignInActivity
import com.example.trelloapp.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessageService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.data.isEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification body: ${it.body}")
        }

        val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
        val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

        sendNotification()
    }

    //instance id is initialized
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Refresh Token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        //TODO: To be implemented
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(title: String, message: String) {
        val intent =
            if (FireStoreClass().getCurrentUserID().isNotEmpty()) {
                Intent(this, MainActivity::class.java)
            }
            else {
                Intent(this, SignInActivity::class.java)
            }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

        val channelID = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundURI =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder =
            NotificationCompat
                .Builder(
                this,
                    channelID
                )
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundURI)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                "Channel ProjeManaG title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}