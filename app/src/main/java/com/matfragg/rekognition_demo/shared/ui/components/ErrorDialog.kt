package com.matfragg.rekognition_demo.shared.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            if (onRetry != null) {
                TextButton(onClick = {
                    onDismiss()
                    onRetry()
                }) {
                    Text("Reintentar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}