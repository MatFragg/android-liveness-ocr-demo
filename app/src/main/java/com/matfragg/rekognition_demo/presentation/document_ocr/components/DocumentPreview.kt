package com.matfragg.rekognition_demo.presentation.document_ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DniPreviewStep(
    frontPath: String?,
    backPath: String?,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Verifica las imágenes", style = MaterialTheme.typography.headlineSmall)

        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ImageItem(path = frontPath, label = "Anverso", Modifier.weight(1f))
            ImageItem(path = backPath, label = "Reverso", Modifier.weight(1f))
        }

        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Check, null)
            Text("Confirmar y subir")
        }

        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, null)
            Text("Repetir captura")
        }
    }
}

@Composable
fun ImageItem(path: String?, label: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        AsyncImage(
            model = path,
            contentDescription = label,
            modifier = Modifier
                .fillMaxWidth()
                .width(150.dp) // Altura fija para el preview
                .clip(RoundedCornerShape(8.dp)),
            // CAMBIA Crop por Fit para ver la imagen real que se enviará
            contentScale = ContentScale.Fit
        )
    }
}