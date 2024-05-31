package com.example.remotedesktop.Helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics

import java.security.AccessController.getContext





class DrawableHelper {
    companion object{
        fun resizeDrawable(image: Drawable, width: Int, height: Int): Drawable {
            val b = (image as BitmapDrawable).bitmap
            val bitmapResized = Bitmap.createScaledBitmap(b, width, height, false)
            return BitmapDrawable(bitmapResized)
        }

        fun Context.pxToDp(px: Int): Int {
            val displayMetrics: DisplayMetrics = resources.getDisplayMetrics()
            return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }

    }
}