package com.matfragg.rekognition_demo.presentation.document_ocr.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.matfragg.rekognition_demo.presentation.document_ocr.scan.ScanStep

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DocumentGuideOverlay(
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor by animateColorAsState(
        targetValue = if (isAligned) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
        animationSpec = tween(300),
        label = "colorAnimation"
    )

    // 📏 BoxWithConstraints detecta el cambio de tamaño físico al rotar
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val isLandscape = screenWidth > screenHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val aspectRatio = 1.586f // DNI Ratio

            // 📐 Lógica Adaptativa
            val rectWidth = if (isLandscape) {
                size.height * 0.75f * aspectRatio // En horizontal nos basamos en el alto
            } else {
                size.width * 0.85f // En vertical nos basamos en el ancho
            }
            val rectHeight = rectWidth / aspectRatio

            val left = (size.width - rectWidth) / 2
            val top = (size.height - rectHeight) / 2

            // 1. Crear el fondo oscuro con el "agujero" recortado
            val overlayPath = androidx.compose.ui.graphics.Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
            }
            val dniPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(left, top, left + rectWidth, top + rectHeight),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                )
            }

            // Operación de diferencia para crear el hueco
            val finalPath = androidx.compose.ui.graphics.Path.combine(
                androidx.compose.ui.graphics.PathOperation.Difference,
                overlayPath,
                dniPath
            )

            drawPath(path = finalPath, color = Color.Black.copy(alpha = 0.6f))

            // 2. Dibujar el marco (Border)
            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )
        }
    }
}