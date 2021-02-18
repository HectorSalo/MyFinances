package com.skysam.hchirinos.myfinances.common.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.NotificationReceiver
import java.io.FileNotFoundException
import kotlin.math.ceil
import kotlin.math.max

object ClassesCommon {

    fun reduceBitmap(
            uri: String?,
            maxAncho: Int,
            maxAlto: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(
                    MyFinancesApp.MyFinancesAppObject.getContext().contentResolver.openInputStream(Uri.parse(uri)),
                    null, options
            )
            options.inSampleSize = max(
                    ceil(options.outWidth / maxAncho.toDouble()),
                    ceil(options.outHeight / maxAlto.toDouble())
            ).toInt()
            options.inJustDecodeBounds = false
            BitmapFactory.decodeStream(
                    MyFinancesApp.MyFinancesAppObject.getContext().contentResolver
                            .openInputStream(Uri.parse(uri)), null, options
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(MyFinancesApp.MyFinancesAppObject.getContext(), R.string.error_image_notfound, Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun createNotification(concepto: String, gasto: Boolean, requestId: Int, fechaInicial: Long) {
        val intent = Intent(MyFinancesApp.appContext, NotificationReceiver::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.BD_CONCEPTO, concepto)
        bundle.putBoolean(Constants.BD_GASTOS, gasto)
        intent.putExtras(bundle)
        val pendingIntent = PendingIntent.getBroadcast(MyFinancesApp.appContext, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = MyFinancesApp.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, fechaInicial, (1000 * 60 * 15).toLong(), pendingIntent)
    }

    fun cancelNotification(requestId: Int) {
        val intent = Intent(MyFinancesApp.appContext, NotificationReceiver::class.java)
        val alarmManager = MyFinancesApp.appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent = PendingIntent.getService(MyFinancesApp.appContext, requestId, intent,
                        PendingIntent.FLAG_NO_CREATE)
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
        }

    }
}