package com.matfragg.rekognition_demo.presentation.reniec

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.matfragg.rekognition_demo.presentation.liveness.ResultRow
import com.matfragg.rekognition_demo.presentation.onboarding.OnboardingState

@Composable
fun ReniecResultScreen(state: OnboardingState) {
    val reniec = state.reniecResult

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Resultado Oficial RENIEC", style = MaterialTheme.typography.headlineSmall)

        // Banner de Estado (HIT / NO HIT)
        val isHit = reniec?.reniecCode == 70006
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isHit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            )
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isHit) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isHit) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isHit) "HIT - PERSONA IDENTIFICADA" else "NO HIT - NO COINCIDE",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHit) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        // Datos del Ciudadano
        ResultCard(title = "Datos del Ciudadano") {
            ResultRow("# DNI", reniec?.documentNumber ?: "-")
            ResultRow("Nombres", reniec?.names ?: "-")
            ResultRow("Apellidos", reniec?.lastNames ?: "-")
            ResultRow("Vencimiento", reniec?.expirationDate ?: "-")
            ResultRow("Nacionalidad", reniec?.nationality ?: "PERUANA")
            ResultRow("Tracking ID", reniec?.trackingToken?.take(15) ?: "-")
        }
    }
}