package com.matfragg.rekognition_demo.presentation.onboarding

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matfragg.rekognition_demo.presentation.liveness.ResultRow
import com.matfragg.rekognition_demo.presentation.onboarding.components.InfoPhotoCard
import com.matfragg.rekognition_demo.presentation.onboarding.components.ProbabilityCircle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FinalResultScreen(state: OnboardingState) {
    val dni = state.dniData
    val result = state.comparisonResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Resultado de la Comparación",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(bottom = 24.dp)
        ) {
            InfoPhotoCard("Foto OCR", dni?.fotoPersonaBase64, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            InfoPhotoCard("Foto Vida", state.livenessPhotoBase64, Modifier.weight(1f))
        }

        ProbabilityCircle(score = result?.similarity ?: 0.0)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Información del Documento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                ResultRow("# DNI", dni?.numeroDni ?: "No detectado")
                ResultRow("Nombres", dni?.nombres ?: "No detectado")
                ResultRow("Apellidos", dni?.apellidos ?: "No detectado")
                ResultRow(
                    label = "Vencimiento",
                    value = dni?.getFormattedVencimiento("dd/MM/yyyy") ?: "No detectado"
                )
                ResultRow("Nacionalidad", dni?.nacionalidad ?: "PERUANA")
            }
        }
    }
}