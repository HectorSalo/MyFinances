package com.skysam.hchirinos.myfinances.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity
import java.util.*

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val concepto = intent!!.getStringExtra("concepto")

        val myIntent = Intent(context, HomeActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //
        //
        val NOTIFICATION_CHANNEL_ID = "OfertaFinish"


        val notificationBuilder = NotificationCompat.Builder(context!!, NOTIFICATION_CHANNEL_ID)
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.rgb(0, 60, 255))
                .setContentTitle("Test")
                .setContentText("Pagar $concepto")
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "Descripcion"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(Random().nextInt(), notificationBuilder.build())

    }
}