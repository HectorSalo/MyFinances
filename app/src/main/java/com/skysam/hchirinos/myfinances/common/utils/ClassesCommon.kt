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
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.*
import com.google.android.material.datepicker.CalendarConstraints.DateValidator
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
import com.skysam.hchirinos.myfinances.common.NotificationReceiverFCM
import java.io.FileNotFoundException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
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

    fun selectDate(fragmentManager: FragmentManager, onClickDatePicker: OnClickDatePicker) {
        val calendarCurrent = Calendar.getInstance()
        val calendarMax = Calendar.getInstance()
        calendarMax.set(calendarCurrent[Calendar.YEAR], 11, 31)
        val calendarMin = Calendar.getInstance()
        calendarMin.set(calendarCurrent[Calendar.YEAR], 0, 1)
        val builder = MaterialDatePicker.Builder.datePicker()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        val constraints = CalendarConstraints.Builder()
        val validators = ArrayList<DateValidator>()
        validators.add(DateValidatorPointForward.from(calendarMin.timeInMillis))
        validators.add(DateValidatorPointBackward.before(calendarMax.timeInMillis))
        constraints.setValidator(CompositeDateValidator.allOf(validators))
        builder.setCalendarConstraints(constraints.build())

        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection: Long? ->
            calendar.timeInMillis = selection!!
            val timeZone = TimeZone.getDefault()
            val offset = timeZone.getOffset(Date().time) * -1
            calendar[Calendar.HOUR_OF_DAY] = calendarCurrent[Calendar.HOUR_OF_DAY]
            calendar[Calendar.MINUTE] = calendarCurrent[Calendar.MINUTE]
            calendar.timeInMillis = calendar.timeInMillis + offset
            onClickDatePicker.date(calendar)
        }
        picker.show(fragmentManager, picker.toString())
    }

    fun convertFloatToString(value: Float): String {
        return String.format(Locale.GERMANY, "%,.2f", value)
    }

    fun convertDoubleToString(value: Double): String {
        return String.format(Locale.GERMANY, "%,.2f", value)
    }

    fun convertDateToCotizaciones(dateString: String): String {
        return try {
            // Formato de entrada basado en el string de fecha recibido
            val inputFormat = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())

            // Parseamos el string a un objeto Date
            val date = inputFormat.parse(dateString)

            // Convertimos el objeto Date al formato local
            DateFormat.getDateTimeInstance().format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            "Invalid date"
        }
    }

    fun createNotification(concepto: String, gasto: Boolean, requestId: Int, fechaInicial: Long) {
        val intent = Intent(MyFinancesApp.appContext, NotificationReceiverFCM::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.BD_CONCEPTO, concepto)
        bundle.putBoolean(Constants.BD_GASTOS, gasto)
        intent.putExtras(bundle)
        val pendingIntent = PendingIntent.getBroadcast(MyFinancesApp.appContext, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = MyFinancesApp.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, fechaInicial, (1000 * 60 * 15).toLong(), pendingIntent)
    }

    fun cancelNotification(requestId: Int) {
        val intent = Intent(MyFinancesApp.appContext, NotificationReceiverFCM::class.java)
        val alarmManager = MyFinancesApp.appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent = PendingIntent.getService(MyFinancesApp.appContext, requestId, intent,
                PendingIntent.FLAG_NO_CREATE)
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}