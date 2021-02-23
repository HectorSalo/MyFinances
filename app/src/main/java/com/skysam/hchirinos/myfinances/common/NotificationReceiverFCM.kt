package com.skysam.hchirinos.myfinances.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity
import java.util.*

class NotificationReceiverFCM: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val myIntent = Intent(this, HomeActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //
        //
        val notificationBuilder = NotificationCompat.Builder(this, Constants.PREFERENCE_NOTIFICATION_CHANNEL_ID)
        notificationBuilder
                .setSmallIcon(R.drawable.ic_trending_up_24)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(Constants.PREFERENCE_NOTIFICATION_CHANNEL_ID,
                    Constants.PREFERENCE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = Constants.PREFERENCE_NOTIFICATION_CHANNEL_DESCRIPTION
            notificationChannel.lightColor = ContextCompat.getColor(this, R.color.colorPrimary)
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(Random().nextInt(), notificationBuilder.build())

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Token", token)
    }
}