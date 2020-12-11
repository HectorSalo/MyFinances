package com.skysam.hchirinos.myfinances.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.common.MyFinancesApp
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
}