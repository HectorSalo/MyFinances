package com.skysam.hchirinos.myfinances.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.utils.Constants
import com.skysam.hchirinos.myfinances.homeModule.ui.HomeActivity
import java.util.*

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val concepto = intent!!.getStringExtra(Constants.BD_CONCEPTO)
        val gasto = intent.getBooleanExtra(Constants.BD_GASTOS, true)

        val icon : Int
        val content: String

        if (gasto) {
            icon = R.drawable.ic_trending_down_24
            content = context!!.getString(R.string.notification_gastos_body, concepto)
        } else {
            icon = R.drawable.ic_trending_up_24
            content = context!!.getString(R.string.notification_ingresos_body, concepto)
        }

        val myIntent = Intent(context, HomeActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, 0, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //
        //
        val notificationBuilder = NotificationCompat.Builder(context, Constants.PREFERENCE_NOTIFICATION_CHANNEL_ID)
        notificationBuilder
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(Constants.PREFERENCE_NOTIFICATION_CHANNEL_ID,
                    Constants.PREFERENCE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = Constants.PREFERENCE_NOTIFICATION_CHANNEL_DESCRIPTION
            notificationChannel.lightColor = ContextCompat.getColor(context, R.color.colorPrimary)
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        notificationManager.notify(Random().nextInt(), notificationBuilder.build())

    }
}