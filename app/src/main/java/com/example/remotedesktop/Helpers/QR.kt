package com.example.remotedesktop.Helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode

fun generateQRCodeImage(
    text: String,
    width: Int,
    height: Int,
): Bitmap {
    val encodingHints: MutableMap<EncodeHintType, Any> = HashMap()
    encodingHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    val code: QRCode = Encoder.encode(text, ErrorCorrectionLevel.H, encodingHints)
    val bitmap: Bitmap = renderQRImage(code, width, height, 4)
    return bitmap
    /*FileOutputStream(filePath).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }*/
}

private fun renderQRImage(code: QRCode, width: Int, height: Int, quietZone: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Set up paint for drawing
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.style = Paint.Style.FILL
    paint.color = Color.BLACK

    // Get the QR code matrix
    val input: ByteMatrix = code.matrix
    if (input == null) {
        throw IllegalStateException()
    }

    // Calculate dimensions
    val inputWidth = input.width
    val inputHeight = input.height
    val qrWidth = inputWidth + (quietZone * 2)
    val qrHeight = inputHeight + (quietZone * 2)
    val outputWidth = Math.max(width, qrWidth)
    val outputHeight = Math.max(height, qrHeight)

    // Calculate scaling factors and padding
    val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
    val leftPadding = (outputWidth - (inputWidth * multiple)) / 2
    val topPadding = (outputHeight - (inputHeight * multiple)) / 2
    val FINDER_PATTERN_SIZE = 7
    val CIRCLE_SCALE_DOWN_FACTOR = 21f / 30f
    val circleSize = (multiple * CIRCLE_SCALE_DOWN_FACTOR).toInt()

    // Iterate through each QR code module
    for (inputY in 0 until inputHeight) {
        var outputY = topPadding
        outputY += multiple * inputY
        for (inputX in 0 until inputWidth) {
            var outputX = leftPadding
            outputX += multiple * inputX
            if (input.get(inputX, inputY).toInt() == 1) {
                if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE ||
                            inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)
                ) {
                    canvas.drawOval(
                        RectF(
                            outputX.toFloat(),
                            outputY.toFloat(),
                            (outputX + circleSize).toFloat(),
                            (outputY + circleSize).toFloat()
                        ),
                        paint
                    )
                }
            }
        }
    }

    // Draw finder patterns
    val circleDiameter = multiple * FINDER_PATTERN_SIZE
    drawFinderPatternCircleStyle(canvas, paint, leftPadding, topPadding, circleDiameter)
    drawFinderPatternCircleStyle(
        canvas,
        paint,
        leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple,
        topPadding,
        circleDiameter
    )
    drawFinderPatternCircleStyle(
        canvas,
        paint,
        leftPadding,
        topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
        circleDiameter
    )

    return bitmap
}

private fun drawFinderPatternCircleStyle(
    canvas: Canvas,
    paint: Paint,
    x: Int,
    y: Int,
    circleDiameter: Int
) {
    val WHITE_CIRCLE_DIAMETER = circleDiameter * 5 / 7
    val WHITE_CIRCLE_OFFSET = circleDiameter / 7
    val MIDDLE_DOT_DIAMETER = circleDiameter * 3 / 7
    val MIDDLE_DOT_OFFSET = circleDiameter * 2 / 7

    paint.color = Color.BLACK
    canvas.drawOval(
        RectF(
            x.toFloat(),
            y.toFloat(),
            (x + circleDiameter).toFloat(),
            (y + circleDiameter).toFloat()
        ),
        paint
    )

    paint.color = Color.WHITE
    canvas.drawOval(
        RectF(
            (x + WHITE_CIRCLE_OFFSET).toFloat(),
            (y + WHITE_CIRCLE_OFFSET).toFloat(),
            (x + WHITE_CIRCLE_OFFSET + WHITE_CIRCLE_DIAMETER).toFloat(),
            (y + WHITE_CIRCLE_OFFSET + WHITE_CIRCLE_DIAMETER).toFloat()
        ),
        paint
    )

    paint.color = Color.BLACK
    canvas.drawOval(
        RectF(
            (x + MIDDLE_DOT_OFFSET).toFloat(),
            (y + MIDDLE_DOT_OFFSET).toFloat(),
            (x + MIDDLE_DOT_OFFSET + MIDDLE_DOT_DIAMETER).toFloat(),
            (y + MIDDLE_DOT_OFFSET + MIDDLE_DOT_DIAMETER).toFloat()
        ),
        paint
    )
}
class QR {

}