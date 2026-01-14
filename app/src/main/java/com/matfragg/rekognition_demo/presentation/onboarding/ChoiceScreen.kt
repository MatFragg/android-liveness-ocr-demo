package com.matfragg.rekognition_demo.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matfragg.rekognition_demo.presentation.onboarding.components.ActionCard

@Composable
fun ChoiceScreen(
    state: OnboardingState,
    onCompareClick: () -> Unit,
    onReniecClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Verificación Completada!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Se capturó exitosamente la foto de vida. ¿Cómo deseas proceder?",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Opción 1: Comparación Facial
        ActionCard(
            title = "Comparador Facial",
            description = "Compara tu foto de vida con la foto registrada en tu DNI",
            icon = Icons.Default.Face,
            onClick = onCompareClick
        )

        // Opción 2: Validación RENIEC
        ActionCard(
            title = "Validación RENIEC",
            description = "Valida tu identidad directamente con la base de datos oficial",
            icon = Icons.Default.AccountBalance,
            onClick = onReniecClick
        )
    }
}